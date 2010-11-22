package mctsbot.strategies;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import mctsbot.gamestate.GameState;
import mctsbot.nodes.AllOpponentsFoldedNode;
import mctsbot.nodes.BotFoldedNode;
import mctsbot.nodes.ChanceNode;
import mctsbot.nodes.LeafNode;
import mctsbot.nodes.Node;
import mctsbot.nodes.PlayerNode;
import mctsbot.nodes.ShowdownNode;

import com.biotools.meerkat.Card;
import com.biotools.meerkat.Deck;
import com.biotools.meerkat.HandEvaluator;

public class StaticDistributionSimulationStrategy implements SimulationStrategy {
	
	private static final int[] FOLD_WEIGHTS = {0, 65, 6, 8, 4};
	private static final int[] CALL_WEIGHTS = {0, 25, 46, 47, 55};
	private static final int[] RAISE_WEIGHTS = {0, 10, 48, 45, 42};
	
	
	private static final Random random = new Random();

	public double simulate(Node node) {
		
		if(node instanceof LeafNode) {
			
			if(node instanceof BotFoldedNode) {
				return node.getParent().getGameState().getBotMoney();
				
			} else if(node instanceof AllOpponentsFoldedNode) {
				return node.getGameState().getBotMoney()+node.getGameState().getPot();
				
			} else if(node instanceof ShowdownNode) {
				return simulateShowdown((ShowdownNode)node);
				
			} else {
				throw new RuntimeException("Unknown node type passed to simulate.");
			}
			
			
		} else if(node instanceof ChanceNode) {
			return simulate(((ChanceNode)node).generateChild());
			
		} else if(node instanceof PlayerNode) {
			
			final boolean canCheck = node.getGameState().getMaxBetThisRound()==0.0;
			final int stage = node.getGameState().getStage();
			
			final double randomDouble = random.nextDouble();
			final double raiseProb = getRaiseProb(stage, canCheck);
			final double callProb = getCallProb(stage, canCheck);
			
			if(randomDouble<raiseProb) {
				return simulate(((PlayerNode)node).createRaiseNode());
				
			} else if(randomDouble<raiseProb+callProb) {
				return simulate(((PlayerNode)node).createCallNode());
				
			} else {
				return simulate(((PlayerNode)node).createFoldNode());
			}

			
		} else {
			throw new RuntimeException("Unknown node type passed to simulate.");
		}
		
	}
	
	
	private double simulateShowdown(ShowdownNode showdownNode) {
		
		final GameState gameState = showdownNode.getGameState();
		
		final int botHandRank = HandEvaluator.rankHand(
				gameState.getC1(), gameState.getC2(), gameState.getTable());
		
		Deck deck = new Deck();
		deck.extractCard(gameState.getC1());
		deck.extractCard(gameState.getC2());
		deck.extractHand(gameState.getTable());
		
		final LinkedList<Card> takenCards = new LinkedList<Card>();
		takenCards.add(gameState.getC1());
		takenCards.add(gameState.getC2());
		for(int i=0; i<gameState.getTable().size(); i++) {
			takenCards.add(gameState.getTable().getCard(i));
		}
		
		final int noOfOpponents = gameState.getNoOfActivePlayers()-1;
		
		int maxOpponentHandRank = 0;
		
		for(int i=0; i<noOfOpponents; i++) {
			
			Card oppC1 = null;
			Card oppC2 = null;
			
			
			
			
			
			final int opponentHandRank = HandEvaluator.rankHand(
					oppC1, oppC2, gameState.getTable());
			
			if(opponentHandRank>maxOpponentHandRank) maxOpponentHandRank = opponentHandRank;
		}
		
		
		double expectedValue = gameState.getBotMoney();
		
		if(botHandRank>=maxOpponentHandRank) {
			expectedValue += gameState.getPot();
		}
		
		return expectedValue;
	}
	
	private Card getRandomOppC1(Collection<Card> takenCards) {
		Card oppC1 = new Card(random.nextInt(52));
		
		
		//TODO
		return null;
	}
	
	private Card getRandomOppC1(List<Card> takenCards, Card oppC1) {
		//TODO
		return null;
	}

	
	protected double getRaiseProb(int stage, boolean canCheck) {
		if(stage<1 || stage>4) throw new RuntimeException(
				"Invalid stage passed to a get prob method: " + stage);
		
		return RAISE_WEIGHTS[stage]/
			(RAISE_WEIGHTS[stage]+CALL_WEIGHTS[stage]+(canCheck?0:FOLD_WEIGHTS[stage]));
	}
	
	protected double getCallProb(int stage, boolean canCheck) {
		if(stage<1 || stage>4) throw new RuntimeException(
				"Invalid stage passed to a get prob method: " + stage);
		
		return CALL_WEIGHTS[stage]/
			(RAISE_WEIGHTS[stage]+CALL_WEIGHTS[stage]+(canCheck?0:FOLD_WEIGHTS[stage]));
	}
	
	protected double getFoldProb(int stage, boolean canCheck) {
		if(stage<1 || stage>4) throw new RuntimeException(
				"Invalid stage passed to a get prob method: " + stage);
		
		return (canCheck?0.0:(FOLD_WEIGHTS[stage]/
			(RAISE_WEIGHTS[stage]+CALL_WEIGHTS[stage]+FOLD_WEIGHTS[stage])));
	}

}



