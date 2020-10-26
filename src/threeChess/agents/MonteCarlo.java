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
   * Copy the last move played or play a Random move
   */
  public Position[] playCopyCatOrRandomMove(Board board){
	  if(Math.random()%2 == 1) {
		  int moveCount = board.getMoveCount();
		  if(moveCount>0) {
			  Position[] lastMove = board.getMove(moveCount-1);
			  try {
				  Position start = Position.get(Colour.values()[(lastMove[0].getColour().ordinal()+1)%3], lastMove[0].getRow(), lastMove[0].getColumn());
				  Position end = Position.get(Colour.values()[(lastMove[1].getColour().ordinal()+1)%3], lastMove[1].getRow(), lastMove[1].getColumn());
				  if(board.isLegalMove(start, end)) return new Position[] {start,end};
			  }
			  catch(ImpossiblePositionException e) {
				  
			  }
		  }
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
		  }catch(ImpossiblePositionException e){
			  
		  }
	  }
	  return new Position[] {start,end};
  }
  
  /*
   * Custom Class for Node
   */
  
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
   
  
  /*
   * Monte Carlo Tree Search algorithm
   */
  public Position[] MonteCarloTreeSearch(Board board){

	  //Initialise with first node
	  Node rootNode = new Node(board);
	  Node chooseNode = null;	  
	  
	  //Initialise move
	  Position[] move = null;
	  
	  move = iterate(chooseNode, rootNode);
	  
	  return move;
  }
  
  /*
   * Helper function for Monte Carlo Tree Search
   */
  public Position[] iterate(Node chooseNode, Node rootNode) {
	
	  //Variable to check number of iterations
	  int iterations = 0;
	  
	  //Iterate 1000 times
	  while(iterations < 1000) {
		  
		  //Find a leaf node with the best UCB score
		  chooseNode = leafSearch(rootNode, rootNode.visits);
	  
		  //Expand the unvisited promising node
		  if(chooseNode.visits > 0) {
			  expandNode(chooseNode);
			  
			  //Set chosen node to the selected child node
			  chooseNode = chooseNode.children.get(0);
		  }
		  
		  //rollout nodes (simulate move)
		  rollOut(chooseNode, rootNode.board.getTurn());
		  
		  //back-propagate node scores
		  backPropagate(chooseNode);
		  iterations++;
	  }
	  //find most visited node
	  Position[] move = chooseMove(rootNode);
	  return move;
  }
  

  
  /*
   * Helper Function to choose move to play
   */
  public Position[] chooseMove(Node rootNode) {
	  int mostVisits = 0;
	  Node currentNode = null;
	  
	  //Iterate through children
	  for(int i = 0; i < rootNode.children.size(); i++) {

		  //Compare if visits on current node is more than most visits
		  if(rootNode.children.get(i).visits > mostVisits) {
			  mostVisits = rootNode.children.get(i).visits;
			  currentNode = rootNode.children.get(i);
		  }
	  }
	  
	  //Check if it is the first move and store the previous move, get the next move
	  if(currentNode.board.getMoveCount() > 0) {
		  Position[] prevMove = currentNode.board.getMove(currentNode.board.getMoveCount() - 1);
		  
		  return setMove(prevMove);
	  }
	  return null;
  }
  
  /*
   * Helper function to set chosen moves
   */
  public Position[] setMove(Position[] move) {
	  try {
		  
		  //Taken from random agent provided
		  Position start = Position.get(Colour.values()[move[0].getColour().ordinal()], move[0].getRow(), move[0].getColumn());
		  Position end = Position.get(Colour.values()[move[1].getColour().ordinal()], move[1].getRow(), move[1].getColumn());
		 
		  Position[] gotMove = {start, end};
		  return gotMove;
	  }
	  catch(ImpossiblePositionException e) {
		  
	  }
	  
	  return null;
  }
  
  /*
   * Helper function to find a leaf node within a tree
   */
  public Node leafSearch(Node node, int parentvisits){
	    Node currentnode = node;
	    while (currentnode.children.size()!= 0){

	      currentnode = UCBcheck(currentnode, parentvisits);
	    }
	    return currentnode;
	  }
  
  /*
   * Helper function to find node with highest UCB value
   */
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
  
  /*
   * Expand a given node
   */
  public void expandNode(Node node){  
	  Position[] piecePositions = node.board.getPositions(node.board.getTurn()).toArray(new Position[0]);
	  
	  //Iterate through all possible piece positions
	  pieceIterate(node, piecePositions);
  }
  
  
  /*
   * Helper function for piece position iteration
   */
  public void pieceIterate(Node node, Position[] piecePositions) {
	  for(int i = 0; i < piecePositions.length; i++) {
		  Position start = piecePositions [i];
		  Position end = piecePositions[i];
		  
		  Piece movePiece = node.board.getPiece(start);
		  Direction[][] directions = movePiece.getType().getSteps();
		  for(int j = 0; j < directions.length; j++) {
			  try {
				  end = node.board.step(movePiece, directions[j], start);
			  }
			  catch(ImpossiblePositionException e) {
				  
			  }
			  repeatExpand(node, movePiece, start, end, directions[j]);
		  }
		  
	  }
  }

  /*
   * Helper function to repeat simulation of moves
   */
  public void repeatExpand(Node node, Piece movePiece, Position start, Position end, Direction[] direction) {
	  int a = -1;
	  Board boardSimulation = null;
	  int reps = movePiece.getType().getStepReps();
	  
	  while(node.board.isLegalMove(start, end) && ++a < reps) {
		  try {
			  boardSimulation = (Board)node.board.clone();
		  }
		  catch(CloneNotSupportedException e) {
			  
		  };
		  try {
			  boardSimulation.move(start, end);
		  }
		  catch(ImpossiblePositionException e) {
			  
		  }
		  Node newNode = new Node(boardSimulation, node);
		  node.children.add(newNode);
		  try {
			  end = node.board.step(movePiece, direction, end);
		  }
		  catch(ImpossiblePositionException e) {
			  
		  };
	  }
  }
  
  /*
   * Roll out move by simulating in a cloned board
   */
  public void rollOut(Node node, Colour playerColour) {
	  Board boardSimulation = null;
	  try {
		  boardSimulation = (Board)node.board.clone();
	  }
	  catch(CloneNotSupportedException e) {
		  
	  };
	  while(boardSimulation.gameOver() != true) {
		  Position[] executeMoves = playCopyCatOrRandomMove(boardSimulation);
		  try {
			  boardSimulation.move(executeMoves[0], executeMoves[1]);
		  }
		  catch(ImpossiblePositionException e) {
			  
		  }
	  }
	  rollOutScore(boardSimulation, node, playerColour);
  }
  
  /*
   * Helper function to help calculate score of a move during roll out
   */
  public void rollOutScore(Board board, Node node, Colour colour) {
	  Colour winner = board.getWinner();
	  if(winner == colour) {
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
  
  /*
   * Helper function to back propagate results+score found from exploration of a node
   */
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


