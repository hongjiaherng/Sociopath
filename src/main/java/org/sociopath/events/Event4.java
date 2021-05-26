package org.sociopath.events;

import java.util.Scanner;
import java.util.Stack;

public class Event4 {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Stack<Integer> stack1 = new Stack<>();
        Stack<Integer> stack2 = new Stack<>();
        int a,b;
        int round=0;
        System.out.print("Enter the number of book: ");
        int n = sc.nextInt();
        // input height of n books
        System.out.print("Enter the height of the books: ");
        for(int i=0; i<n; i++){
            stack1.push(sc.nextInt());
        }

        while(true){
            int stack1size = stack1.size();

            while(!stack1.isEmpty()) {
                a = stack1.pop();
                b = 0;

                if (!stack1.isEmpty()) {
                    b = stack1.peek();
                }

                if(a<b || stack1.isEmpty()){
                    stack2.push(a);
                }
            }

            if(stack1size == stack2.size()){
                break;
            }

            // Push Back height of books into stack1
            while(!stack2.isEmpty()){
                stack1.push(stack2.pop());
            }
            // increment round
            round++;
        }
        System.out.println("Rounds needed to make the height in non-increasing order: " + round);
    }
}
