package sara.apps.core.games;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import sara.Applet;
import sara.apps.core.Game;

@Game(name="Solitaire",description="Classic solitaire card game")
public class Solitaire extends Applet {
	
	/*
	 * 
	 *  000| 111  222  333  444  555  666  777  AAA
	 *  					   
	 *                         BB
	 *                         
	 *                         CC
	 *  
	 *  		               DD
	 * 
	 *  
	 *  
	 *  
	 */                        

	public Solitaire(Applet parent, String[] args) {
		super(parent, 45, 22);
//		setFont(getFont().deriveFont(1));
		LCDMode = true;
		
		deck = new Card[52];
		for(int i = 1, pos = 0; i < 14; i++) {
			deck[pos++] = new Card(i, Card.HEARTS);
			deck[pos++] = new Card(i, Card.DIAMONDS);
			deck[pos++] = new Card(i, Card.CLUBS);
			deck[pos++] = new Card(i, Card.SPADES);
		}
	}
	
	private void newGame() {
		shuffle(10);
		hand = new LinkedList<>();
		for(Card x : deck) hand.add(x);
		for(int i = 0; i < 7; i++) {
			stacks[i] = new LinkedList<>();
			for(int j = 0; j <= i; j++) {
				stacks[i].add(hand.remove());
				stacks[i].getLast().faceup = false;
			}
			stacks[i].getLast().faceup = true;
		}
		for(int i = 0; i < 4; i++) {
			outs[i] = new LinkedList<>();
		}
		for(Card c : hand) c.faceup = true;
	}

	@Override
	protected void onInit() {
		newGame();
	}

	@Override
	protected void onUpdate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onClose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void keyPressed(int code, char c) {
		switch(code) {
			case KeyEvent.VK_N:
				newGame();
				break;
			case KeyEvent.VK_LEFT:
				if(stackSelect > -1 + selectMode) {
					stackSelect--;
					
					if(stackSelect != -1)
						hSelect = stacks[stackSelect].size() - (selectMode != 1 ? 1 : 0);
				}
				break;
			case KeyEvent.VK_RIGHT: {
				if(stackSelect < 6) {
					stackSelect++;
					
					hSelect = stacks[stackSelect].size() - (selectMode != 1 ? 1 : 0);
				}
				break;
			}
			case KeyEvent.VK_UP: {
				if(selectMode == 0 && stackSelect >= 0 && hSelect > 0 && stacks[stackSelect].get(hSelect-1).faceup) hSelect--;
				break;
			}
			case KeyEvent.VK_DOWN: {
				if(selectMode == 0 && stackSelect >= 0 && hSelect < stacks[stackSelect].size()-1) hSelect++;
				break;
			}
			case KeyEvent.VK_ENTER: {
				switch(selectMode) {
					case 0: {
						fromStk = stackSelect;
						
						if(stackSelect == -1) {
							selected = hand.getFirst();
						} else {
							selected = stacks[stackSelect].get(hSelect);
							fromH = hSelect;
						}
						
						selectMode = 1;
						
						break;
					}
					case 1: {
						if(stackSelect == fromStk || !selectedFits()) break;
						
						if(fromStk != -1) {
							ListIterator<Card> i = stacks[fromStk].listIterator(fromH);
							while(i.hasNext()) {
								stacks[stackSelect].add(i.next());
								i.remove();
							}
							if(stacks[fromStk].size() > 0) stacks[fromStk].getLast().faceup = true;
						} else {
							stacks[stackSelect].add(selected);
							hand.remove();
						}
						
						selected = null;
						fromStk = -2;
						fromH = -1;
						selectMode = 0;
						hSelect--;
						break;
					}
				}
				break;
			}
			case KeyEvent.VK_ESCAPE: {
				if(selectMode == 1) {
					selected = null;
					fromStk = -2;
					fromH = -1;
					selectMode = 0;
					if(stackSelect != fromStk) hSelect--;
				} else {
					close();
					return;
				}
				break;
			}
			case KeyEvent.VK_SHIFT: {
				selected = null;
				fromStk = -2;
				fromH = -1;
				selectMode = 0;
				hand.add(hand.remove());
				break;
			}
			case KeyEvent.VK_F: {
				if(selectMode != 0) break;
				
				if(stackSelect == -1) {
					selected = hand.getFirst();
					if(fitsOut()) {
						outs[selected.suit].add(selected);
						hand.remove();
					}
				} else {
					if(hSelect == stacks[stackSelect].size()-1) {
						selected = stacks[stackSelect].get(hSelect);
						if(fitsOut()) {
							outs[selected.suit].add(selected);
							stacks[stackSelect].removeLast();
							if(stacks[stackSelect].size() > 0) stacks[stackSelect].getLast().faceup = true;
							hSelect--;
						}
					}
				}
				
				selected = null;
				
				break;
			}
		}
		
		refreshDisplay();
	}
	
	private boolean fitsOut() {
		if(outs[selected.suit].size() == 0) return selected.num == Card.ACE;
		return outs[selected.suit].getLast().num+1 == selected.num;
	}
	
	private boolean selectedFits() {
		if(stacks[stackSelect].size() == 0) return selected.num == Card.KING;
		Card c = stacks[stackSelect].getLast();
		return c.num-1 == selected.num && c.suit >= 2 ^ selected.suit >= 2;
	}
	
	int stackSelect = -1, hSelect = 0, selectMode = 0;
	int fromStk = -2, fromH = -1;
	
	private static final char[] title = "SOLITAIRE".toCharArray(), vic = "VICTORY".toCharArray();
	
	@Override
	protected void refreshDisplay() {
		rootDisplay.clear();
		
		rootDisplay.writeln(title, 0, 18);
		
		if(!hand.isEmpty()) {
			rootDisplay.writeln(String.valueOf(hand.size()).toCharArray(), 1, 0);
			
			drawCard(hand.getFirst(), 3, 0);
		} else {
			rootDisplay.writeln(vic, 21, 19);
		}
		
		for(int i = 0; i < stacks.length; i++) {
			drawStack(stacks[i], 1, 6+5*i);
		}
		
		for(int i = 0; i < 4; i++) {
			if(!outs[i].isEmpty())
				drawCard(outs[i].getLast(), 1+2*i, 42);
		}
		
		if(stackSelect == -1)
			rootDisplay.color(Color.BLACK, Color.WHITE, 3, 0, 3);
		else if(stackSelect != fromStk)
			rootDisplay.color(Color.BLACK, Color.WHITE, hSelect+1, 6+5*stackSelect, 3);
		
//		System.out.println(hSelect);
//		System.out.println(6+5*stackSelect);
		
		super.refreshDisplay();
	}
	
	private void drawCard(Card c, int line, int col) {
		drawCard(c, c == selected, line, col);
	}
	
	private void drawCard(Card c, boolean shift, int line, int col) {
		rootDisplay.writeln(c.faceup ? c.out : Card.unknown, line, col);
		rootDisplay.color(c.faceup && c.suit < 2 ? Color.RED : null, shift ? Color.GRAY : null, line, col, 3);
	}
	
	private void drawStack(List<Card> cards, int line, int col) {
		boolean shift = false;
		for(Card c : cards) {
			shift = c == selected ? true : shift;
			drawCard(c, shift, line++, col);
		}
	}
	
	Card selected;
	
	Card[] deck;
	LinkedList<Card> hand;
	
	@SuppressWarnings("unchecked")
	final LinkedList<Card>[] stacks = (LinkedList<Card>[]) new LinkedList<?>[7],
			outs = (LinkedList<Card>[]) new LinkedList<?>[4];
	
	void shuffle(int amt) {
		Random rng = new Random();
		amt *= 52;
		
		for(int i = 0; i < amt; i++) {
			swap(rng.nextInt(52), rng.nextInt(52));
		}
	}
	
	private void swap(int i, int j) {
		Card temp = deck[i];
		deck[i] = deck[j];
		deck[j] = temp;
	}

	public static class Card {
		public final int suit; // heart diamond club spade 0-3
		public final int num; // 1 = a ... 11 = j, 12 = q, 13 = k
		private final char[] out;
		
		private static final char[] unknown = new char[] { '?','?','?' };
		
		public static final int HEARTS = 0, DIAMONDS = 1, CLUBS = 2, SPADES = 3;
		public static final int ACE = 1, JACK = 11, QUEEN = 12, KING = 13;
		
		public boolean faceup = true;
		
		public Card(int num, int suit) {
			this.suit = suit;
			this.num = num;
			
			out = new char[3];
			if(num > 1 && num < 10) {
				out[0] = (char) (0x30 + num);
			} else {
				switch(num) {
					case 1: out[0] = 'A'; break;
					case 10: out[0] = '1'; out[1] = '0'; break;
					case 11: out[0] = 'J'; break;
					case 12: out[0] = 'Q'; break;
					case 13: out[0] = 'K'; break;
					default: out[0] = '?'; break;
				}
			}
			switch(suit) {
				case HEARTS: out[2] = '\u2665'; break;
				case DIAMONDS: out[2] = '\u2666'; break;
				case CLUBS: out[2] = '\u2663'; break;
				case SPADES: out[2] = '\u2660'; break;
				default: out[2] = '?'; break;
			}
		}
	}
}
