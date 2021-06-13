package org.sociopath.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Event6 {
    static Scanner sc = new Scanner(System.in);

    public static void event6() {
        int n = sc.nextInt();
        Graph g = new Graph(n+1);

        for(int i=0; i<n; i++){
            int s = sc.nextInt();
            int d = sc.nextInt();
            g.addEdge(s,d);
        }

        System.out.println("You can form the following friendship (s): ");
        for(int i=1; i<=n; i++){
            for(int j=1; j<=n; j++){
                if(i!=j){
                    g.printAllPath(i,j);
                }
            }
        }
        System.out.println(g.clearPath());
    }
}

class Graph {

    // No. of vertices in graph
    private int v;

    // Adjacency List
    private ArrayList<Integer>[] adjList;
    private List<ArrayList<Integer>> path = new ArrayList<>();
    private ArrayList<ArrayList<Integer>> tempPath = new ArrayList<>();

    public Graph(int vertices){
        // initialise vertex count
        this.v = vertices;

        // initialise adjacency list
        initAdjList();
    }

    // Utility method to initialise adjacency list
    @SuppressWarnings("unchecked")
    public void initAdjList(){
        adjList = new ArrayList[v];
        for(int i=0; i<v; i++){
            adjList[i] = new ArrayList<>();
        }
    }

    public void addEdge(int u, int v){
        // Add v to u's list
        adjList[u].add(v);
        adjList[v].add(u);
    }

    // Print all paths
    public void printAllPath(int s, int d){
        boolean[] isVisited = new boolean[v];
        ArrayList<Integer> pathList = new ArrayList<>();

        // add source to path[]
        pathList.add(s);

        // Call recursive utility
        printAllPathsUtil(s,d,isVisited,pathList);
    }

    // A recursive function to print all path from 'u' to 'd'
    // isVisited[] keeps track of vertices in current path.
    // localPathList<> stores actual vertices in the current path
    private void printAllPathsUtil(Integer u, Integer d, boolean[] isVisited, List<Integer> localPathList){
        if(u.equals(d)){
            String temp1 = localPathList.toString();
            temp1 = temp1.substring(1,temp1.length()-1);
            String[] temp2 = temp1.split(", ");
            ArrayList<Integer> temp3 = new ArrayList<>();
            for(String i: temp2){
                Integer temp4 = Integer.parseInt(i);
                temp3.add(temp4);
            }
            path.add(temp3);
            tempPath.add(temp3);
            // if match found then no need to traverse more till depth
            return;
        }

        // Mark the current node
        isVisited[u] = true;

        // Recur for all the vertices adjacent to current vertex
        for(Integer i : adjList[u]){
            if(!isVisited[i]){
                // Store current node in path[]
                localPathList.add(i);
                printAllPathsUtil(i,d,isVisited,localPathList);
                // Remove current node in path[]
                localPathList.remove(i);
            }
        }
        // Mark the current node
        isVisited[u] = false;
    }

    public String clearPath(){
        for(int i=0; i<path.size(); i++){
            for(int j=0; j<path.size(); j++){
                if(i!=j){
                    int[] arr1 = new int[path.get(i).size()];
                    for(int k=0; k<path.get(i).size(); k++){
                        arr1[k] = path.get(i).get(k);
                    }
                    int[] arr2 = new int[path.get(j).size()];
                    int l=0;
                    for(int k=path.get(j).size()-1; k>=0; k--){
                        arr2[l] = path.get(j).get(k);
                        l++;
                    }
                    if(Arrays.equals(arr1,arr2)){
                        path.remove(j);
                    }
                }
            }
        }
        ArrayList<Integer> temp = new ArrayList<>();
        for(int i=0; i<path.size()-1; i++){
            for(int j=0; j<path.size()-1-i; j++){
                if(path.get(j).size()>path.get(j+1).size()){
                    temp = path.get(j);
                    path.remove(j);
                    path.add(j,path.get(j));
                    path.remove(j+1);
                    path.add(j+1,temp);
                }
            }
        }
        String str = "";
        for(int i=0; i<path.size(); i++){
            str += (i+1) + ". " + path.get(i) + "\n";
        }
        return str;
    }
}