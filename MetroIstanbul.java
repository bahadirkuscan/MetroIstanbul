import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class MetroIstanbul {
    public static void main(String[] args) throws FileNotFoundException {
        Scanner input1 = new Scanner(System.in);
        String start = input1.next();
        Scanner input2 = new Scanner(System.in);
        String destination = input2.next();
        File file = new File("coordinates.txt");
        Scanner reader = new Scanner(file);

        String[] metro_line_names = new String[10];
        int[][] metro_line_rgb_values = new int[10][3];  // line --> colors
        String[][] metro_station_names = new String[10][];  // line --> stations
        int[][][] metro_station_coords = new int[10][][]; // line --> station --> coordinates
        String[] breakpoint_names = new String[7];
        String[][] breakpoint_metro_lines = new String[7][]; // breakpoint --> lines
        String[] visible_names = new String[0]; // station names to print to canvas
        int counter1 = -1;  // counter for metro line declaration lines
        int counter2 = 0;  // counter for breakpoint lines



        while (reader.hasNext()) {   // Main loop for data taking
            String line = reader.nextLine();
            String[] line_elements = line.split(" ");


            if (line_elements.length == 2) {   // 2 elements means the line read states a metro line since a metro line can't have a singular station or a breakpoint can't have a singular line
                counter1 += 1;
                metro_line_names[counter1] = line_elements[0];
                String[] rgb_vals = line_elements[1].split(",");  // split red green blue
                for (int i = 0; i < 3; i++) {
                    metro_line_rgb_values[counter1][i] = Integer.parseInt(rgb_vals[i]);  // store rgb values
                }

            } else if (!line.contains(",")) {   // if there is no comma in the line then the line read states a breakpoint
                breakpoint_names[counter2] = line_elements[0];  // store breakpoint names
                int connected_line_count = line_elements.length - 1;
                breakpoint_metro_lines[counter2] = new String[connected_line_count];
                for (int i = 0; i < connected_line_count; i++) {
                    breakpoint_metro_lines[counter2][i] = line_elements[i + 1]; // store line names
                }
                counter2 += 1;


            } else {  // last possibility is the stations
                int counter3 = -1; // counter for stations
                int station_count = line_elements.length / 2; // half are names half are coordinates
                metro_station_names[counter1] = new String[station_count];
                metro_station_coords[counter1] = new int[station_count][2];

                for (int i = 0; i < line_elements.length; i++) {
                    if (i % 2 == 0) {  // indicates station names
                        counter3 += 1;
                        String station = line_elements[i];
                        if (station.startsWith("*")) { // means visible name
                            metro_station_names[counter1][counter3] = station.substring(1);
                            visible_names = arrayAppendString(visible_names, station.substring(1));
                        } else {
                            metro_station_names[counter1][counter3] = station;
                        }

                    } else { // indicates coordinates
                        String coords = line_elements[i];
                        String[] x_and_y = coords.split(",");
                        for (int k = 0; k < 2; k++) {
                            metro_station_coords[counter1][counter3][k] = Integer.parseInt(x_and_y[k]); // store coordinates
                        }
                    }
                }
            }
        }
        reader.close();


        boolean correct_start_input = false;
        boolean correct_destination_input = false;
        for (String[] stations : metro_station_names){  // check if station exists
            if (arrayContains(stations,start)){
                correct_start_input = true;
            }
            if (arrayContains(stations,destination)){
                correct_destination_input = true;
            }
        }

        if (correct_start_input && correct_destination_input) {  // inputs are correct
            String[] initial_array = new String[0];
            String[] path = pathFinder(start, destination, metro_line_names, metro_station_names, breakpoint_names, breakpoint_metro_lines, initial_array);
            if (path.length == 1) {   // no connection case (only the start station exists in path)
                System.out.println("These two stations are not connected");
            }


            else {   // everything went right, path is found
                StdDraw.setCanvasSize(1024,482);
                StdDraw.setXscale(0,1024);
                StdDraw.setYscale(0,482);
                double PEN_RADIUS_LINE = 0.012;
                double PEN_RADIUS_STATION = 0.01;
                StdDraw.setFont(new Font("Helvetica", Font.BOLD, 8));
                double CURRENT_POINT_PEN_RADIUS = 0.02;
                int PAUSE_DURATION = 300;


                for (String station : path) {  // console output
                    System.out.println(station);
                }

                String[] passed_stations = new String[0]; // canvas output
                StdDraw.enableDoubleBuffering();

                for (String station : path){
                    canvasDrawer(passed_stations,station,metro_line_rgb_values,metro_station_names,metro_station_coords,visible_names,PEN_RADIUS_LINE,PEN_RADIUS_STATION,CURRENT_POINT_PEN_RADIUS);
                    StdDraw.show();
                    passed_stations = arrayAppendString(passed_stations,station);
                    StdDraw.pause(PAUSE_DURATION);
                }
            }
        }


        else{    // inputted station(s) does not exist
            System.out.println("The station names provided are not present in this map.");
        }
    }


    private static boolean arrayContains(String[] array, String element) {
        for (String i : array) {
            if (i.equals(element)) {
                return true;
            }
        }
        return false;
    }

    private static String[] arrayAppendString(String[] array, String element) {
        int len = array.length;
        String[] new_array = new String[len + 1];
        for (int i = 0; i < len; i++) {
            new_array[i] = array[i];
        }
        new_array[len] = element;
        return new_array;
    }



    private static int arrayFindIndex(String[] array, String element) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(element)) {
                return i;
            }
        }
        return -1;
    }

    // main recursive call to find path
    private static String[] pathFinder(String start, String destination, String[] metro_line_names, String[][] metro_station_names, String[] breakpoint_names, String[][] breakpoint_metro_lines, String[] construction_array) {
        construction_array = arrayAppendString(construction_array, start); // add the current station to the path
        int line_index = 0;
        int station_index = 0;
        String[] solution;

        for (int i = 0; i < 10; i++) {  // getting the position of the current station in terms of array indexes
            if (arrayContains(metro_station_names[i], start)) {
                line_index = i;
                for (int k = 0; k < metro_station_names[i].length; k++) {
                    if (metro_station_names[i][k].equals(start)) {
                        station_index = k;
                        break;
                    }
                }
                break;
            }
        }

        if (start.equals(destination)) { // reached destination
            return construction_array;
        }

        if (arrayContains(breakpoint_names, start)) { // breakpoint case
            int breakpoint_index = 0;
            for (int i = 0; i < 7; i++) {
                if (breakpoint_names[i].equals(start)) {
                    breakpoint_index = i;
                    break;
                }
            }

            if ((station_index - 1 >= 0)) {   // calling station on left
                if (!arrayContains(construction_array, metro_station_names[line_index][station_index - 1])) {
                    solution = pathFinder(metro_station_names[line_index][station_index - 1], destination, metro_line_names, metro_station_names, breakpoint_names, breakpoint_metro_lines, construction_array);
                    if (solution[solution.length - 1].equals(destination)) { // check if destination was reached in later calls
                        return solution;
                    }
                }
            }
            if (station_index + 1 < metro_station_names[line_index].length) {  // calling station on right
                if (!arrayContains(construction_array, metro_station_names[line_index][station_index + 1])) {
                    solution = pathFinder(metro_station_names[line_index][station_index + 1], destination, metro_line_names, metro_station_names, breakpoint_names, breakpoint_metro_lines, construction_array);
                    if (solution[solution.length - 1].equals(destination)) {
                        return solution;
                    }
                }
            }

            for (String line : breakpoint_metro_lines[breakpoint_index]) { // calling the neighbours in all the lines accessible in the breakpoint
                int new_line_index = arrayFindIndex(metro_line_names, line);
                if (new_line_index == line_index) { // already called the neighbours in the current line so we can skip it
                    continue;
                }
                int new_station_index = arrayFindIndex(metro_station_names[new_line_index], start);
                if (new_station_index - 1 >= 0) {
                    if (!arrayContains(construction_array, metro_station_names[new_line_index][new_station_index - 1])) { // station on left in new line
                        solution = pathFinder(metro_station_names[new_line_index][new_station_index - 1], destination, metro_line_names, metro_station_names, breakpoint_names, breakpoint_metro_lines, construction_array);
                        if (solution[solution.length - 1].equals(destination)) {
                            return solution;
                        }
                    }
                }
                if (new_station_index + 1 < metro_station_names[new_line_index].length) {
                    if (!arrayContains(construction_array, metro_station_names[new_line_index][new_station_index + 1])) { // station on right in new line
                        solution = pathFinder(metro_station_names[new_line_index][new_station_index + 1], destination, metro_line_names, metro_station_names, breakpoint_names, breakpoint_metro_lines, construction_array);
                        if (solution[solution.length - 1].equals(destination)) {
                            return solution;
                        }
                    }
                }
            }


        }
        else {    // not a breakpoint
            if ((station_index - 1 >= 0)) {   // calling station on left
                if (!arrayContains(construction_array, metro_station_names[line_index][station_index - 1])) {
                    solution = pathFinder(metro_station_names[line_index][station_index - 1], destination, metro_line_names, metro_station_names, breakpoint_names, breakpoint_metro_lines, construction_array);
                    if (solution[solution.length - 1].equals(destination)) {
                        return solution;
                    }
                }
            }
            if (station_index + 1 < metro_station_names[line_index].length) {  // calling station on right
                if (!arrayContains(construction_array, metro_station_names[line_index][station_index + 1])) {
                    solution = pathFinder(metro_station_names[line_index][station_index + 1], destination, metro_line_names, metro_station_names, breakpoint_names, breakpoint_metro_lines, construction_array);
                    if (solution[solution.length - 1].equals(destination)) {
                        return solution;
                    }
                }
            }
        }

        return construction_array; // correct path was not found, all calls return the path until that station but only the first call matters here which return just the beginning station
        }


    private static void canvasDrawer(String[] passed_stations, String current_station, int[][] metro_line_rgb_values,  String[][] metro_station_names, int[][][] metro_station_coords, String[] visible_names, double PEN_RADIUS_LINE, double PEN_RADIUS_STATION, double CURRENT_POINT_PEN_RADIUS){
        StdDraw.clear(); // clear the canvas
        StdDraw.picture(512,241, "background.jpg"); // set the background


        StdDraw.setPenRadius(PEN_RADIUS_LINE); // draw the metro lines by using all stations with their next neighbour in all lines
        for (int i = 0 ; i < 10 ; i++){
            StdDraw.setPenColor(metro_line_rgb_values[i][0],metro_line_rgb_values[i][1],metro_line_rgb_values[i][2]);
            int station_count = metro_station_coords[i].length;
            for (int k = 0 ; k < station_count - 1 ; k++){
                StdDraw.line(metro_station_coords[i][k][0],metro_station_coords[i][k][1],metro_station_coords[i][k+1][0],metro_station_coords[i][k+1][1]);
            }
        }

        StdDraw.setPenRadius(PEN_RADIUS_STATION);

        for (int i = 0 ; i < 10 ; i++){
            for (int k = 0 ; k < metro_station_names[i].length ; k++){  // looping through all the stations

                if (metro_station_names[i][k].equals(current_station)){ // current station
                    StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);
                    StdDraw.setPenRadius(CURRENT_POINT_PEN_RADIUS);
                    StdDraw.filledCircle(metro_station_coords[i][k][0], metro_station_coords[i][k][1],4);
                    StdDraw.setPenRadius(PEN_RADIUS_STATION);
                }
                else if (arrayContains(passed_stations,metro_station_names[i][k])){  // passed station
                    StdDraw.setPenColor(StdDraw.PRINCETON_ORANGE);
                    StdDraw.filledCircle(metro_station_coords[i][k][0], metro_station_coords[i][k][1],2);
                }

                else{   // normal station
                    StdDraw.setPenColor(Color.WHITE);
                    StdDraw.filledCircle(metro_station_coords[i][k][0], metro_station_coords[i][k][1],2);
                }
                if (arrayContains(visible_names,metro_station_names[i][k])){ // drawing station names if they are meant to be visible
                    StdDraw.setPenColor(Color.BLACK);
                    StdDraw.text(metro_station_coords[i][k][0], metro_station_coords[i][k][1] + 5, metro_station_names[i][k]);
                }
            }
        }
        }
    }


