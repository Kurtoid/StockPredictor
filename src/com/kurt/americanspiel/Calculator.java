package com.kurt.americanspiel;

import java.util.Scanner;

public class Calculator {

	public static void main(String[] args) {

		// Scannerjus
		Scanner plus = new Scanner(System.in);

		// Welcome Name
		while (true) {

			try {


				System.out.print("Enter your name to start: ");
				String name = plus.next();
				if (name.toLowerCase().equals("justin")) {
					{

						System.out.print("Welcome " + name);

						System.out.println("\n");
					}


					// Enter Operation
					while (true) {


						System.out.print("Enter add, subtract, multiply, divide, square, squareroot, or exit to exit: ");
						String op = plus.next();
						switch (op) {
							case "exit":


								System.exit(0);
							case "add":
								// Addition

								System.out.print("Enter number to add: ");
								double addA = plus.nextDouble();

								System.out.print("Enter number to add to previous number: ");
								double addB = plus.nextDouble();

								double addAnswer = addA + addB;

								System.out.print(addA + " + " + addB + " = " + addAnswer);

								System.out.println("\n");
								break;
							case "subtract":

								// Subtraction
								System.out.print("Enter number to subtract: ");
								double sub = plus.nextDouble();

								System.out.print("Enter number to subtract from previous number: ");
								double subA = plus.nextDouble();

								double subAnswer = sub - subA;

								System.out.print(sub + " - " + subA + " = " + subAnswer);

								System.out.println("\n");
								break;
							case "multiply":

								// Multiplication
								System.out.print("Enter number to multiply: ");
								double mult = plus.nextDouble();

								System.out.print("Enter number to multiply by previous number: ");
								double multA = plus.nextDouble();

								double multAnswer = mult * multA;

								System.out.print(mult + " * " + multA + " = " + multAnswer);

								System.out.println("\n");
								break;
							case "divide":

								// Division
								System.out.print("Enter number to divide: ");
								double div = plus.nextDouble();

								System.out.print("Enter number to divide by previous number: ");
								double divA = plus.nextDouble();

								double divAnswer = div / divA;

								System.out.print(div + " / " + divA + " = " + divAnswer);

								System.out.println("\n");
								break;
							case "square":
								// Square
								System.out.print("Enter a number to square: ");
								double square = plus.nextDouble();

								System.out.print(square + " squared is " + (square * square));

								System.out.println("\n");
								break;
							case "squareroot":

								// Square Root
								System.out.print("Enter a number to square root: ");
								double sqrt = plus.nextDouble();

								System.out.print(sqrt + " square rooted is " + Math.sqrt(sqrt));

								System.out.println("\n");
								break;
							default:
								System.out.println("Try again");
								break;
						}

						// Error Message

					}
				}

			} catch (java.util.InputMismatchException e) {

				System.out.println("You entered a letter or a symbol! Try a number next time.");
			}

		}
	}
}
