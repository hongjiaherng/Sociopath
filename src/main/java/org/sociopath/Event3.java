package org.sociopath;

import java.util.ArrayList;

public class Event3 {

    public static void event3(Sociograph sociograph, String hostName){
        int n = 0;
        ArrayList<Student> myLunchMates = new ArrayList<>();
        ArrayList<Student> list = sociograph.getAllStudents();
        list.remove(sociograph.getStudent(hostName));
        Student host = sociograph.getStudent(hostName);

        host.setLunchStart(12,00);
        host.setLunchPeriod(180);
        host.setLunchEnd(14,00);

        System.out.println("My lunch time");
        System.out.println("==============");
        System.out.println(host);
        System.out.println();

        if(list.isEmpty()){
            return;
        }
        else{
            for (Student student : list) {
                if(n > 7) {// i dont know why need 7, if i use 3 i will produce one lunchMate only
                    continue;
                }
                else{
                    double newRepPoint = 0;
                    if (student.getDive() <= 50) {
                        if (host.getLunchStart().isAfter(student.getLunchStart())) {
                            continue;
                        } else if (host.getLunchStart().isBefore(student.getLunchStart())) {
                            if (host.getLunchEnd().isAfter(student.getLunchEnd())) {
                                myLunchMates.add(student);
                                if (sociograph.hasDirectedEdge(hostName, student.getName())) {
                                    newRepPoint = host.getRepPoints().get(student.getName()) + 1;
                                    sociograph.setDirectedEdgeWeight(hostName, student.getName(), newRepPoint);
                                } else {
                                    sociograph.addDirectedEdge(hostName, student.getName(), 1);
                                }
                            }
                        }
                    }
//                    n++;
                }
                n++;
            }
            System.out.println("My lunch mates");
            System.out.println("==============");
            for(Student mate : myLunchMates) {
                System.out.println(mate);
                System.out.println();
            }
        }
    }
}
