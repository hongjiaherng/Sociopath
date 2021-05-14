import java.util.LinkedList;

/**
 * @author Lim Hong Zhi, Hong Jia Herng
 *
 * A student class that is used for our Sociopath graph
 *
 * @version v1.0
 * - added the basic method and variables
 * - have an idea wanted to add name as a variable
 * since there is nothing to differentiate between all the
 * student objects
 */
public class Student {

    private int rep;
    private double dive;
    private int lunchStart;
    private int lunchPeriod;
    private final LinkedList<Student> friends;

    /**
     * Constructor for the Student class
     *
     * Minimum arguments are reputation (rep), diving rate(dive),
     * lunch starting time (lunchStart), lunch period(lunchPeriod)
     */
    public Student(int rep, double dive, int lunchStart, int lunchPeriod) {
        this.rep = rep;
        this.dive = dive;
        this.lunchStart = lunchStart;
        this.lunchPeriod = lunchPeriod;
        friends = new LinkedList<>();
    }

    public Student(int reputation, double dive, int lunchStart, int lunchPeriod, LinkedList<Student> friends) {
        this.rep = reputation;
        this.dive = dive;
        this.lunchStart = lunchStart;
        this.lunchPeriod = lunchPeriod;
        this.friends = friends;
    }

    public int getRep() {
        return rep;
    }

    /**
     * Getter and setters for all the variables
     * **except for the list of friends, it has a getter only
     */
    public void setRep(int rep) {
        this.rep = rep;
    }

    public double getDive() {
        return dive;
    }

    public void setDive(double dive) {
        this.dive = dive;
    }

    public int getLunchStart() {
        return lunchStart;
    }

    public void setLunchStart(int lunchTime) {
        this.lunchStart = lunchTime;
    }

    public int getLunchPeriod() {
        return lunchPeriod;
    }

    public void setLunchPeriod(int lunchPeriod) {
        this.lunchPeriod = lunchPeriod;
    }

    public LinkedList<Student> getFriends(){
        return friends;
    }

    /**
     * A method to add a new friend for the student
     * @param friend a student object which is considered as a friend
     *               of the student
     * @return true if it is added successfully, false if it is not
     */
    public boolean addFriends(Student friend){
        boolean result = friends.add(friend);
        if(result){
            System.out.println("Successfully added a friend!");
            return true;
        }

        else{
            System.out.println("unable to add a friend");
            return false;
        }
    }

    /**
     * A toString method to print out the object
     */
    public String toString(){

        return "Reputation : " + rep +
                "\nDiving rate : " + dive +
                "\nLunch Time : " + lunchStart +
                "\nLunch Period : " + lunchPeriod +
                "\nFriend list: " + friends;
    }
}
