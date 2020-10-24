package threeChess.agents;

import java.util.*;
import threeChess.*;

import java.util.Random;

/**
 * An interface for AI bots to implement.
 * They are simply given a Board object indicating the positions of all pieces, 
 * the history of the game and whose turn it is, and they respond with a move, 
 * expressed as a pair of positions.
 * **/ 
public class MonteCarlo extends Agent{
  
  private static final String name = "MonteCarlo";
  private static final Random random = new Random();


  /**
   * A no argument constructor, 
   * required for tournament management.
   * **/
  public MonteCarlo(){
  }

  
  /*
  public List<State> getAllPossibleStates(){
      // constructs a list of all possible states from current state

  }
  */
  

  /**
   * Play a move in the game. 
   * The agent is given a Board Object representing the position of all pieces, 
   * the history of the game and whose turn it is. 
   * They respond with a move represented by a pair (two element array) of positions: 
   * the start and the end position of the move.
   * @param board The representation of the game state.
   * @return a two element array of Position objects, where the first element is the 
   * current position of the piece to be moved, and the second element is the 
   * position to move that piece to.
   * **/
  
  //Class for Node
  public class Node{
	  Board board;
	  Node parentNode;
	  ArrayList<Node> children;
	  int score;
	  int visits;
	  
	  // Constructors for Nodes
	  
	  //First Node has no parent
	  Node(Board board){
		  this.board = board;
		  this.children = new ArrayList<Node>();
		  this.score = 0;
		  this.visits = 0;
	  }
	  
	  //Other nodes require parents
	  Node(Board board, Node parentNode){
		  this.board = board;
		  this.parentNode = parentNode;
		  this.children = new ArrayList<Node>();
		  this.score = 0;
		  this.visits = 0;
	  }
  }
    
  
  
  public Position[] MonteCarloTreeSearch(Board board){

	  //initialise with first node
	  Node rootNode = new Node(board);
	  
	  //initialise move
	  Position[] move = null;
	  
	  /*	  
	  Define end time -> iterations?
	  */
	  
	  int iterations = 0;
	  
	  while(iterations < 100) {
		  System.out.println(iterations + "/100 iterations");
		  
		  //select a promising node
		  
		  iterations++;
	  }
	  
	  //find most visited node
	  int mostVisits = 0;
	  
	  Node currentNode = null;
	  
	  //Iterate through nodes
	  int numberOfChildren = rootNode.children.size();
	  
	  for(int i = 0; i < numberOfChildren; i++) {
		  Node child = rootNode.children.get(i);
		  
		  //Compare if visits on current node is more than most visits
		  if(child.visits > mostVisits) {
			  mostVisits = child.visits;
			  currentNode = child;
		  }
	  }
	  
	  int moveCount = currentNode.board.getMoveCount();
	  
	  if(moveCount > 0) {
		  Position[] lastMove = currentNode.board.getMove(moveCount - 1);
		  
		  try {
			  
			  //Taken from random agent provided
			  Position start = Position.get(Colour.values()[lastMove[0].getColour().ordinal()], lastMove[0].getRow(), lastMove[0].getColumn());
			  Position end = Position.get(Colour.values()[lastMove[1].getColour().ordinal()], lastMove[1].getRow(), lastMove[1].getColumn());
			  return new Position[] {start,end};
		  }
		  catch(ImpossiblePositionException e) {}
	  }
	  
	  return null;
  }
  
  
  
  
  public Position[] playCopyCatMove(Board board){
	  int moveCount = board.getMoveCount();
	  if(moveCount>0) {
		  Position[] lastMove = board.getMove(moveCount-1);
		  try {
			  Position start = Position.get(Colour.values()[(lastMove[0].getColour().ordinal()+1)%3], lastMove[0].getRow(), lastMove[0].getColumn());
			  Position end = Position.get(Colour.values()[(lastMove[1].getColour().ordinal()+1)%3], lastMove[1].getRow(), lastMove[1].getColumn());
			  if(board.isLegalMove(start, end)) return new Position[] {start,end};
		  }
		  catch(ImpossiblePositionException e) {}
	  }
	  Position[] pieces = board.getPositions(board.getTurn()).toArray(new Position[0]);
	  Position start = pieces[0];
	  Position end = pieces[0]; //dummy illegal move
	  while (!board.isLegalMove(start, end)){
		  start = pieces[random.nextInt(pieces.length)];
		  Piece mover = board.getPiece(start);
		  Direction[][] steps = mover.getType().getSteps();
		  Direction[] step = steps[random.nextInt(steps.length)];
		  int reps = 1 + random.nextInt(mover.getType().getStepReps());
		  end = start;
		  try{
		    for(int i = 0; i<reps; i++)
		      end = board.step(mover, step, end, start.getColour()!=end.getColour());
		  }catch(ImpossiblePositionException e){}
	  }
	  return new Position[] {start,end};
  }
	
  /**
   * @return the Agent's name, for annotating game description.
   **/ 
  public String toString(){return name;}
	
  /**
   * Displays the final board position to the agent, 
   * if required for learning purposes. 
   * Other a default implementation may be given.
   * @param finalBoard the end position of the board
   * **/
  public void finalBoard(Board finalBoard){}
}


