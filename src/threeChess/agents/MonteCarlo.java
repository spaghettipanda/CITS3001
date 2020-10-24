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
	  Node currentNode = null;	  
	  
	  //initialise move
	  Position[] move = null;
	  
	  //Variable to check number of iterations
	  int iterations = 0;
	  
	  //Iterate 100 times
	  while(iterations < 100) {
		  System.out.println(iterations + "/100 iterations");
		  
		  //Find a leaf node with the best UCB score
		  while(currentNode.children.size() != 0) {
			  double UCBScore = 0;
			  double bestUCBScore = Integer.MIN_VALUE;
			  
			  Node bestUCBNode = null;
			  
			  for(int i = 0; i < rootNode.children.size(); i++) {
				  if(rootNode.children.get(i).visits == 0) {
					  currentNode = rootNode.children.get(i);
				  }
				  UCBScore = ((double)rootNode.children.get(i).score/(double)rootNode.children.get(i).visits)+1.41*Math.sqrt(Math.log(rootNode.visits)/(double)rootNode.children.get(i).visits);
				  if(UCBScore > bestUCBScore) {
					  bestUCBScore = UCBScore;
					  bestUCBNode = rootNode.children.get(i);
				  }
			  }
			  currentNode = bestUCBNode;
		  }  
		  
		  //Expand the unvisited promising node
		  if(currentNode.visits > 0) {
			  expandNode(currentNode);
			  
			  currentNode = currentNode.children.get(0);
		  }
		  
		  //rollout(currentNode, rootnode.board.getTurn());
		  //backpropagate(currentNode);
		  iterations++;
	  }
	  
	  //find most visited node
	  int mostVisits = 0;
	  
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
  
  /*public Node UCBcheck(Node node, int pVisits) {
	  double UCBScore = 0; 
	  double bestUCBScore = Integer.MIN_VALUE;
	  Node bestUCBNode = null;
	  for(int i = 0; i < node.children.size(); i++) {
		  if(node.children.get(i).visits == 0) {
			  return node.children.get(i);
		  }
		  UCBScore = ((double)node.children.get(i).score/(double)node.children.get(i).visits)+1.41*Math.sqrt(Math.log(pVisits)/(double)node.children.get(i).visits);
		  if(UCBScore > bestUCBScore) {
			  bestUCBScore = UCBScore;
			  bestUCBNode = node.children.get(i);
		  }
	  }
	  return bestUCBNode;
  }*/
  
  
  
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


