package main;

import java.util.Arrays;

/**
 * @author Polina Shlepakova
 */
public class PostingZones implements Comparable<PostingZones> {

    public enum Zone {
        TITLE       (0.35), // 0
        AUTHOR      (0.25), // 1
        LANGUAGE    (0.05), // 2
        ENCODING    (0.05), // 3
        TRANSLATOR  (0.1),  // 4
        CONTENT     (0.2);  // 5

        private final double weight;

        Zone(double weight) {
            this.weight = weight;
        }

        public double getWeight() {
            return this.weight;
        }
    }

    @Override
    public int compareTo(PostingZones that) {
        return Double.compare(this.getWeight(), that.getWeight());
    }

    public double getWeight() {
        double sum = 0;
        for (Zone z : zones) {
            sum += z.getWeight();
        }
        return sum;
    }

    private int ID;
    private Zone[] zones;

    public int getID() {
        return ID;
    }

    public static int getID(String postingStr) {
        // partition ID and list of zones
        String[] input = postingStr.split("\\.");
        return Integer.parseInt(input[0]);
    }

    public Zone[] getZones() {
        Zone[] zonesCopy = new Zone[zones.length];
        System.arraycopy(zones, 0, zonesCopy, 0, zones.length);
        return zonesCopy;
    }

    public Zone getZone() {
        return zones[0];
    }

    public static final Zone[] VALUES = Zone.values();

    public PostingZones(int ID, Zone zone) {
        this.ID = ID;
        this.zones = new Zone[1];
        this.zones[0] = zone;
    }

    public PostingZones(int ID, Zone[] zones) {
        this.ID = ID;
        this.zones = zones;
    }

    public PostingZones(String str) {
        // partition ID and list of zones
        String[] input = str.split("\\.");
        this.ID = Integer.parseInt(input[0]);
        // partition list of zones
        input = input[1].split("\\,");
        this.zones = new Zone[input.length];
        for (int i = 0; i < input.length; i++) {
            this.zones[i] = VALUES[Integer.parseInt(input[i])];
        }
    }

    public void addZone(Zone zone) {
        for (Zone z : zones) {
            if (z == zone) {
                return;
            }
        }
        Zone[] temp = new Zone[this.zones.length + 1];
        System.arraycopy(this.zones, 0, temp, 0, this.zones.length);
        this.zones = temp;
        this.zones[this.zones.length - 1] = zone;
        Arrays.sort(this.zones);
    }

    /**
     * Posting with zones is represented as a String like this:<br>
     * <code>ID.zone1,zone2,zone3</code><br>
     * For example, for a posting with id 2 and zones AUTHOR and TITLE,
     * String representation looks like this:<br>
     * <code>2.1,0</code>
     *
     * @return String representation of this Posting
     */
    public String toString() {
        String res = "" + ID + "." + zones[0].ordinal();
        for (int i = 1; i < zones.length; i++) {
            res += "," + zones[i].ordinal();
        }
        return res;
    }

    public String zonesToString() {
        String res = "" + ((zones.length > 0) ? zones[0].toString() : "");
        for (int i = 1; i < zones.length; i++) {
            res += ", " + zones[i].toString();
        }
        return res;
    }

    public static PostingZones and(PostingZones p1, PostingZones p2) {
        if (p1.getID() != p2.getID()) {
            return null;
        }
        Zone[] zones1 = p1.getZones();
        Zone[] zones2 = p2.getZones();
        Zone[] intersection = new Zone[VALUES.length];
        int index = 0;
        for (int i = 0, j = 0; i < zones1.length && j < zones2.length; ) {
            if (zones1[i].ordinal() == zones2[j].ordinal()) {
                intersection[index++] = zones1[i];
                i++;
                j++;
            } else if (zones1[i].ordinal() < zones2[j].ordinal()) {
                i++;
            } else {
                j++;
            }
        }
        if (index == 0) {
            return null;
        }
        Zone[] res = new Zone[index];
        System.arraycopy(intersection, 0, res, 0, index);
        return new PostingZones(p1.getID(), res);
    }

    public static void main(String[] args) {
        PostingZones p1 = new PostingZones(2, Zone.CONTENT);
        p1.addZone(Zone.TITLE);
        p1.addZone(Zone.TITLE);
        p1.addZone(Zone.AUTHOR);
        System.out.println("p1: " + p1);
//        String postingStr = p1.toString();
//        System.out.println(postingStr);
//        PostingZones p2 = new PostingZones(postingStr);
//        System.out.println(p2);
        PostingZones p2 = new PostingZones(2, Zone.AUTHOR);
        p2.addZone(Zone.TITLE);
        p2.addZone(Zone.LANGUAGE);
        p2.addZone(Zone.CONTENT);
        System.out.println("p2: " + p2);

        PostingZones intersection = PostingZones.and(p1, p2);
        System.out.println("intersection: " + intersection);
    }
}
