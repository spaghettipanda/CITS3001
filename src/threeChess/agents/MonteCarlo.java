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
	  while(iterations <= 5000) {
		  System.out.println(iterations + "/5000 iterations");
		  
		  //Find a leaf node with the best UCB score
		  currentNode = pickLeafNode(rootNode, rootNode.visits);
	  
		  //Expand the unvisited promising node
		  if(currentNode.visits > 0) {
			  expandNode(currentNode);
			  
			  currentNode = currentNode.children.get(0);
		  }
		  
		  rollOut(currentNode, rootNode.board.getTurn());
		  backPropagate(currentNode);
		  iterations++;
	  }
	  //find most visited node
	  return chooseMove(rootNode);
  }

  
  public Position[] chooseMove(Node rootNode) {
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
  
  public Node pickLeafNode(Node node, int parentvisits){
	    Node currentnode = node;
	    while (currentnode.children.size()!= 0){

	      currentnode = UCBcheck(currentnode, parentvisits);
	    }
	    return currentnode;
	  }
  
  public Node UCBcheck(Node node, int pVisits) {
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
  }
  
  public void expandNode(Node node){
	  Board initialBoard = node.board;
	  
	  Position[] piecePositions = initialBoard.getPositions(node.board.getTurn()).toArray(new Position[0]);
	  for(int i = 0; i < piecePositions.length; i++) {
		  Position start = piecePositions [i];
		  Position end = piecePositions[i];
		  
		  Piece movePiece = initialBoard.getPiece(start);
		  Direction[][] directions = movePiece.getType().getSteps();
		  int reps = movePiece.getType().getStepReps();
		  for(int j = 0; j < directions.length; j++) {
			  try {
				  end = initialBoard.step(movePiece, directions[j], start);
			  }
			  catch(ImpossiblePositionException e) {}
			  repeatSteps(node, movePiece, start, end, directions[j]);
		  }
		  
	  }
  }

  public void repeatSteps(Node node, Piece movePiece, Position start, Position end, Direction[] direction) {
	  int a = -1;
	  Board boardSimulation = null;
	  int reps = movePiece.getType().getStepReps();
	  
	  while(node.board.isLegalMove(start, end) && ++a < reps) {
		  try {
			  boardSimulation = (Board)node.board.clone();
		  }
		  catch(CloneNotSupportedException e) {};
		  try {
			  boardSimulation.move(start, end);
		  }
		  catch(ImpossiblePositionException e) {}
		  Node newNode = new Node(boardSimulation, node);
		  node.children.add(newNode);
		  try {
			  end = node.board.step(movePiece, direction, end);
		  }
		  catch(ImpossiblePositionException e) {};
	  }
  }
  
  
  public void rollOut(Node node, Colour playerColour) {
	  Board boardSimulation = null;
	  try {
		  boardSimulation = (Board)node.board.clone();
	  }
	  catch(CloneNotSupportedException e) {};
	  while(boardSimulation.gameOver() != false) {
		  Position[] executeMoves = playCopyCatMove(boardSimulation);
		  try {
			  boardSimulation.move(executeMoves[0], executeMoves[1]);
		  }
		  catch(ImpossiblePositionException e) {}
	  }
	  Colour winner = boardSimulation.getWinner();
	  if(winner == playerColour) {
		  node.score = 1;
	  }
	  else {
		  if(winner == null) {
			  node.score = 0;
		  }
		  else {
			  node.score = -1;
		  }
	  }
  }
  
  public void backPropagate(Node node) {
	  node.visits++;
	  int score = node.score;
	  Node currentNode = node;
	  
	  while(currentNode.parentNode != null) {
		  currentNode = currentNode.parentNode;
		  currentNode.score += score;
		  currentNode.visits++;
	  }
  }
  
  public Position[] playCopyCatMove(Board board){
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
  
  public Position[] playMove(Board board) {
	  return MonteCarloTreeSearch(board);
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


