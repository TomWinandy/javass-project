package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SplittableRandom;

import ch.epfl.javass.Preconditions;
import ch.epfl.javass.jass.Card.Color;

public final class MctsPlayer implements Player {
    private final PlayerId ownId;
    private final SplittableRandom rng;
    private final int iterations;

    private static final int CONSTANT = 40;

    /**
     * Constructs a simulated player with the Monte Carlo Tree Search algorithm.
     * This simulated player is constructed with the given random seed, the
     * given player and the given number of iteration.
     * 
     * @param ownId
     *            the given player
     * @param rngSeed
     *            the random seed
     * @param iterations
     *            the number of iterations on the MCTS algorithm
     */
    public MctsPlayer(PlayerId ownId, long rngSeed, int iterations) {
        Preconditions.checkArgument(iterations >= Jass.HAND_SIZE);
        this.ownId = ownId;
        rng = new SplittableRandom(rngSeed);
        this.iterations = iterations;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {

        // If there is only one playable card, it is trivially played.
        if (state.trick().playableCards(hand).size() == 1) {
            return state.trick().playableCards(hand).get(0);
        }

        // The root of the Monte Carlo Tree is created
        Node root = new Node(state, hand, ownId);

        // The MCTS algorithm is iterated "iterations" times
        for (int i = 0; i < iterations; i++) {
            // A node is attempted to be added to the Monte Carlo Tree and all
            // the path to the Node is obtained
            List<Node> nodePath = addNode(root);
            // The new node is obtained
            Node nodeToSimulate = nodePath.get(nodePath.size() - 1);
            // The Score of the randomly ended turn is obtained using the state
            // of the Node
            Score score = scoreOfFinishedTurn(nodeToSimulate.turnState,
                    nodeToSimulate.playerHand);
            // The scores of the parent nodes of the simulated Node are updated
            updateScores(nodePath, score);
        }

        // The most promising card is returned.
        return state.unplayedCards().difference(
                root.children[root.bestChildIndex(0)].turnState.unplayedCards())
                .get(0);
    }
    
    @Override
    public Color selectTrump(Random trumpRng) {
        return Card.Color.ALL.get(trumpRng.nextInt(Card.Color.COUNT));
    }

    // Updates the scores of the nodes in nodePath according to the added score.
    private void updateScores(List<Node> nodePath, Score score) {
        nodePath.get(0).updateScore(score, ownId);
        for (int i = 1; i < nodePath.size(); i++) {
            nodePath.get(i).updateScore(score,
                    nodePath.get(i - 1).turnState.nextPlayer());
        }
    }

    // Tells which card are playable according to the turnstate, the hand and
    // the player's identity.
    private static CardSet playableCards(TurnState turnState, CardSet hand,
            PlayerId ownId) {
        if (turnState.nextPlayer().equals(ownId)) {
            return turnState.trick().playableCards(hand);
        } else {
            return turnState.trick()
                    .playableCards(turnState.unplayedCards().difference(hand));
        }
    }

    // Tries to add a node at the position determined by the MCTS algorithm.
    private List<Node> addNode(Node root) {
        // We create the List to memorize the path from the root to the Node to
        // simulate
        List<Node> nodePath = new ArrayList<>();
        // And we add the root to this path
        nodePath.add(root);
        boolean extremity = false;
        Node lastChild = root;
        // We go through the tree, each time one level deeper, until we can add
        // a Node, or we reached the bottom
        while (!extremity) {
            int bestChildIndex = lastChild.bestChildIndex(CONSTANT);
            // We go out of the while if we are at the bottom of the tree
            if (lastChild.turnState.trick().isLast() && lastChild.turnState
                    .trick().size() == PlayerId.COUNT - 1) {
                break;
            } else if (lastChild.children[bestChildIndex] == null) {
                extremity = true;
                Card card = lastChild.notImplementedChildren.get(0);
                lastChild.notImplementedChildren = lastChild.notImplementedChildren
                        .remove(card);
                lastChild.children[bestChildIndex] = new Node(
                        lastChild.turnState
                                .withNewCardPlayedAndTrickCollected(card),
                        lastChild.playerHand.remove(card), ownId);
                Node newChild = lastChild.children[bestChildIndex];
                nodePath.add(newChild);
            } else {
                lastChild = lastChild.children[bestChildIndex];
                nodePath.add(lastChild);
            }
        }
        return nodePath;
    }

    // Simulates the game until the end of the turn, in order to determine a
    // score.
    private Score scoreOfFinishedTurn(TurnState turnState, CardSet playerHand) {
        while (!turnState.isTerminal()) {
            CardSet playableCards = playableCards(turnState, playerHand, ownId);
            Card card = playableCards.get(rng.nextInt(playableCards.size()));
            turnState = turnState.withNewCardPlayedAndTrickCollected(card);
            playerHand = playerHand.remove(card);
        }
        return turnState.score();
    }

    // Nested class that represents the node of a Monte Carlo tree.
    private static class Node {
        private final TurnState turnState;
        private final Node[] children;
        private CardSet notImplementedChildren;
        private int totalPoints;
        private int totalTurns;
        private final CardSet playerHand;

        // Constructs a Node.
        private Node(TurnState turnState, CardSet hand, PlayerId ownId) {
            this.turnState = turnState;
            notImplementedChildren = playableCards(turnState, hand, ownId);
            children = new Node[notImplementedChildren.size()];
            totalPoints = 0;
            totalTurns = 0;
            playerHand = hand;
        }

        // Determines the index of the best child of a node.
        private int bestChildIndex(int c) {
            double maxValue = 0;
            int maxNodeIndex = 0;
            for (int i = 0; i < children.length; i++) {
                double value = value(c, children[i]);
                if (maxValue < value) {
                    maxValue = value;
                    maxNodeIndex = i;
                }
            }
            return maxNodeIndex;
        }

        // Determines the value of a node.
        private double value(int c, Node n) {
            if (n == null) {
                return Double.POSITIVE_INFINITY;
            } else {
                return n.totalPoints * 1.0 / n.totalTurns + c
                        * Math.sqrt(2.0 * Math.log(totalTurns) / n.totalTurns);
            }
        }

        // Updates the score of a Node.
        private void updateScore(Score score, PlayerId playerToPlay) {
            totalTurns++;
            totalPoints += score.turnPoints(playerToPlay.team());
        }
    }
}