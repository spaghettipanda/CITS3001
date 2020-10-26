package threeChess.agents;

import threeChess.*;

import java.util.Random;
import java.util.ArrayList;

/**
 * An interface for AI bots to implement.
 * They are simply given a Board object indicating the positions of all pieces, 
 * the history of the game and whose turn it is, and they respond with a move, 
 * expressed as a pair of positions.
 * **/ 
public class sh extends Agent{
  
  private static final String name = "sh";
  private static final Random random = new Random();


  /**
   * A no argument constructor, 
   * required for tournament management.
   * **/
  public sh(){
  }


  public class Node {
    Board board;
    Node parent;
    int totalscore;
    int visits;
    ArrayList<Node> children;

    Node(Board board, Node parent){
      this.board = board;
      this.parent = parent;
      this.children = new ArrayList<Node>();
      this.totalscore = 0;
      this.visits = 0;
    }

    //Create the first node of a tree. Has no parent
    Node(Board board){
      this.board = board;
      this.children = new ArrayList<Node>();
      this.totalscore = 0;
      this.visits = 0;
    }

  }


  public Position[] mcts(Board board) {
    //initialise the start of mcts
    int iterations = 1000;
    Node rootnode = new Node(board);
    Node currentNode = rootnode;

    expandNode(rootnode);
    while(iterations > 0){
      //Selection either returns the current node if it's a leaf node or changes current node to the child node of current that maximises UCB1
      currentNode = pickLeafNode(rootnode, rootnode.visits);

      //expand that node if the ni value is 0
      if(currentNode.visits != 0){
        
        expandNode(currentNode);
        //System.out.println(currentNode.children.size() + "@@@");
        currentNode = currentNode.children.get(0);
      }
      //perform rollout on that node
      
      rollOut(currentNode, rootnode.board.getTurn());
      backpropagate(currentNode);
      
      //backpropagate from this current node's score
      

      iterations--;
    }
    return pickPosition(rootnode);
  }

  public Position[] pickPosition(Node rootnode){
    //goes through the rootnode's children
    int highestvisits = Integer.MIN_VALUE;
    Node chosennode = null;
    //System.out.println("rootnodechild is " + rootnode.children.size());
    for(int i = 0; i < rootnode.children.size(); i++){
      Node child = rootnode.children.get(i);
     // System.out.println(child.visits + " " + child.totalscore );
      if(child.visits > highestvisits){
        highestvisits = child.visits;
        chosennode = child;
      }
    }
    int moveCount = chosennode.board.getMoveCount();
    if(moveCount>0){
      Position[] lastMove = chosennode.board.getMove(moveCount-1);
      //System.out.println(lastMove[0].getColour() + "@@@@@ " + lastMove[1].getColour());
     
      try{
        Position start = Position.get(Colour.values()[(lastMove[0].getColour().ordinal())], lastMove[0].getRow(), lastMove[0].getColumn());
        Position end = Position.get(Colour.values()[(lastMove[1].getColour().ordinal())], lastMove[1].getRow(), lastMove[1].getColumn());
       // System.out.println(start+ "@@@@@ " + end);
      // if(chosennode.board.isLegalMove(start,end)){
        System.out.println("legal");
         return new Position[] {start, end};
     // }
        
         // return new Position[] {start, end};
       // else{
        //  System.out.println("NOOOOOOOOOOOOOOO");
       // }
      } catch(ImpossiblePositionException e){
        //System.out.println("the move you wanted to play is impossible");
      }
    }
    
    return null;
  }

  public Node pickLeafNode(Node node, int parentvisits){
    Node currentnode = node;
    while (currentnode.children.size()!= 0){

      currentnode = bestUCBNode(currentnode, parentvisits);
    }
    return currentnode;
  }
  
  public Node bestUCBNode(Node node, int parentvisits){
    double bestUCB = Integer.MIN_VALUE;
    Node bestUCBnode = null;
    for(int i = 0; i < node.children.size(); i++){
      Node childnode = node.children.get(i);
      if(childnode.visits == 0){return childnode;}
      double newUCB = calculateUCB(childnode.visits, childnode.totalscore, parentvisits);
      if(newUCB > bestUCB){
        bestUCB = newUCB;
        bestUCBnode = childnode;
      }
    }
    return bestUCBnode;
  }

  public double calculateUCB(double visits, double totalscore, double parentvisits){
    return ((double) totalscore/ (double) visits) + 1.41* Math.sqrt(Math.log(parentvisits) / (double) visits);
  }

  public void expandNode(Node node){
    Board initboard = node.board;
    Board boardclone = null;    
    Position[] pieces = initboard.getPositions(node.board.getTurn()).toArray(new Position[0]);
    for(int i = 0; i < pieces.length; i++){
      Position start = pieces[i];
      Position end = pieces[i];
      Piece mover = initboard.getPiece(start);
      Direction[][] steps = mover.getType().getSteps();
      int reps = mover.getType().getStepReps();     
      for(int c = 0; c < steps.length; c++){
        try{
        end = initboard.step(mover, steps[c], start);}catch(ImpossiblePositionException e){}
        int j = -1;
        //if(initboard.isLegalMove(start,end)){
        //System.out.println("boardclone's move is legal or not" + initboard.isLegalMove(start,end) +start + "- " + end + "reps" + steps.length);
        //try{initboard.move(start, end);}catch(ImpossiblePositionException e){}
        //}
        //Node newnode = new Node(boardclone, node);
        //node.children.add(newnode);
        while(initboard.isLegalMove(start, end)&& ++j < reps){
          try{boardclone = (Board)initboard.clone();} catch     (CloneNotSupportedException e){};
          try{
            boardclone.move(start,end);}catch(ImpossiblePositionException e){
              System.out.println(e);
            }
            Node newnode = new Node(boardclone, node);
            node.children.add(newnode);
          try{end = initboard.step(mover, steps[c], end);}catch(ImpossiblePositionException e){}; 
         //System.out.println("whileloop" + node.children.size());     
          
        }
      }
      }   
  }



  //aim of rollout is to simulate random play until there is a terminal state
  //so we get the node we want to perform rollout on.
  //we get the copy of the board
  //we keep simulating random play on it until gameover = true
  //we return a value of the terminal state.

  public void rollOut(Node node, Colour treePlayer){
    Board boardclone = null;
    try{boardclone = (Board)node.board.clone();} catch(CloneNotSupportedException e){};
    while(!boardclone.gameOver()){
      Position[] randommove = playRandomMove(boardclone);
      try{
      boardclone.move(randommove[0], randommove[1]);
      }catch(ImpossiblePositionException e){}
    }
    if(boardclone.getWinner() == treePlayer){
      node.totalscore =  1;
      
   
    }else if(boardclone.getWinner() == null){
      node.totalscore = 0;
      
    }
    else{
      node.totalscore = -1;
    }
  }

  public Position[] playRandomMove(Board board){
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

  public void backpropagate(Node node){
    //given a node
    //we have to track back to it's parent, and add the score
    node.visits++;
    int nodescore = node.totalscore;
    Node currentnode = node;
    while(currentnode.parent != null){
      currentnode = currentnode.parent;
      currentnode.totalscore += nodescore;
      currentnode.visits++;
    }
  }
 
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
  public Position[] playMove(Board board){
    return mcts(board);
  }

  /**
   * @return the Agent's name, for annotating game description.
   * **/ 
  public String toString(){return name;}

  /**
   * Displays the final board position to the agent, 
   * if required for learning purposes. 
   * Other a default implementation may be given.
   * @param finalBoard the end position of the board
   * **/
  public void finalBoard(Board finalBoard){}

}

