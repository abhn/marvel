public class DMatch {
    // Member variables
    private int queryIdx;  // Query descriptor index
    private int trainIdx;  // Train descriptor index
    private int imgIdx;    // Train image index
    private float distance; // Distance between descriptors

    // Default constructor initializing to default values
    public DMatch() {
        this.queryIdx = -1;
        this.trainIdx = -1;
        this.imgIdx = -1;
        this.distance = Float.MAX_VALUE;
    }

    // Constructor with query and train indices and distance
    public DMatch(int queryIdx, int trainIdx, float distance) {
        this.queryIdx = queryIdx;
        this.trainIdx = trainIdx;
        this.imgIdx = -1;
        this.distance = distance;
    }

    // Constructor with query and train indices, image index, and distance
    public DMatch(int queryIdx, int trainIdx, int imgIdx, float distance) {
        this.queryIdx = queryIdx;
        this.trainIdx = trainIdx;
        this.imgIdx = imgIdx;
        this.distance = distance;
    }

    // Method to compare this DMatch with another DMatch
    public boolean isLessThan(DMatch other) {
        return this.distance < other.distance;
    }

    // Method to print the DMatch details
    public void print() {
        System.out.println("DMatch [queryIdx=" + queryIdx 
                           + ", trainIdx=" + trainIdx 
                           + ", imgIdx=" + imgIdx 
                           + ", distance=" + distance + "]");
    }

    public static void main(String[] args) {
        // Example usage of DMatch
        DMatch match1 = new DMatch(0, 1, 0.5f);
        DMatch match2 = new DMatch(1, 2, 0.3f);

        match1.print();
        match2.print();

        if (match1.isLessThan(match2)) {
            System.out.println("Match1 is closer than Match2.");
        } else {
            System.out.println("Match2 is closer than Match1.");
        }
    }
}
