package com.kurt.americanspiel;

import java.util.Scanner;

public class Calculator {
  
  public static void main(String[]args){
    
    // Scanner
    Scanner plus = new Scanner(System.in);
    
    // Welcome Name
    while(true)  
    {
      
      try 
      {
        
        if(name.equals("Justin")){
          System.out.print("Enter your name to start: ");
          String name = plus.next();
          {
      
            System.out.print("Welcome " + name);
    
            System.out.println(" ");
            System.out.println(" ");
          }
          
    
          if(System.exit(0))
   
            // Enter Operation
            while(true)  
          {
      
            try 
            {
        
              System.out.print("Enter add, subtract, multiply, divide, square, squareroot, or exit to exit: ");
              String op = plus.next();
              if(op.equals("exit"))
              {
                
                System.exit(0);  
              }
              
              // Addition
              if (op.equals("add")) {
                System.out.print("Enter number to add: ");
                double addA = plus.nextDouble();
                
                System.out.print("Enter number to add to previous number: ");
                double addB = plus.nextDouble();
                
                double addAnswer = addA + addB;
                
                System.out.print(addA + " + " + addB + " = " + addAnswer);
                
                System.out.println(" ");
                System.out.println(" ");   
              } 
              
              else if (op.equals("subtract"))
              {
                
                // Subtraction
                System.out.print("Enter number to subtract: ");
                double sub = plus.nextDouble();
                
                System.out.print("Enter number to subtract from previous number: ");
                double subA = plus.nextDouble();
                
                double subAnswer = sub - subA;
                
                System.out.print(sub + " - " + subA + " = " + subAnswer);
                
                System.out.println(" ");
                System.out.println(" "); 
              } 
              
              else if (op.equals("multiply"))
              {
                
                // Multiplication
                System.out.print("Enter number to multiply: ");
                double mult = plus.nextDouble();
                
                System.out.print("Enter number to multiply by previous number: ");
                double multA = plus.nextDouble();
                
                double multAnswer = mult * multA;
                
                System.out.print(mult + " * " + multA + " = " + multAnswer);
                
                System.out.println(" ");
                System.out.println(" "); 
              } 
              
              else if (op.equals("divide")) 
              {
                
                // Division
                System.out.print("Enter number to divide: ");
                double div = plus.nextDouble();
                
                System.out.print("Enter number to divide by previous number: ");
                double divA = plus.nextDouble();
                
                double divAnswer = div / divA;
                
                System.out.print(div + " / " + divA + " = " + divAnswer);
                
                System.out.println(" ");
                System.out.println(" ");  
              }
              
              else if (op.equals("square"))
              {
                // Square
                System.out.print("Enter a number to square: ");
                double square = nextDouble();
                
                System.out.print(square + " squared is " + (square * square));
                
                System.out.println(" ");
                System.out.println(" ");
              }
              
              else if (op.equals("squareroot"))
              {
                
                // Square Root
                System.out.print("Enter a number to square root: ");
                double sqrt = nextDouble();
                
                System.out.print(sqrt + " square rooted is " + (sqrt / sqrt));
                
                System.out.println(" ");
                System.out.println(" ");
              }
           
            }
            
            // Error Message
            catch(java.util.InputMismatchException e)
            {
              
              System.out.println("You entered a letter or a symbol! Try a number next time."); 
            }  
          }
        }
      }             
    }
  }
}