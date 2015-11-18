import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TemperaturesPlotter extends PApplet {



//libraries used
//import java.lang.reflect.Array;


Serial SerialPort;    //The serial port

//Variables
//Config this to connect to the board. COM ports and baudrate
int baudrate = 115200;
String COMPORT = "COM51";
int MAXTEMPERATURE = 300;
int TARGETTEMPERATURE = 250;

int graphMargin = 40;
int sizeX = 800;
int sizeY = 640;
int graphSpaceX = sizeX - 2 * graphMargin;
int graphSpaceY = sizeY - 2 * graphMargin;
int incrementXPosition = 2;
int axisDivisions = 29;
int axisDivisionLenght = 3;
int temperatureIncrementAxis = MAXTEMPERATURE / (axisDivisions + 1); 

int[] lastxPos = {graphMargin, graphMargin, graphMargin, graphMargin, graphMargin, graphMargin};
int[] lastheight = {600, 600, 600, 600, 600, 600};
int[] xPos = {graphMargin, graphMargin, graphMargin, graphMargin, graphMargin, graphMargin};
String inString;
float[] temperatureList = new float[6];
int[] mappedTemperatures = new int[6];

int startTime, finishTime, heatingTime;

//Colors. Every hotendhas a color to differentiate it
int hotend1 = color(4,79,111);
int hotend2 = color(84,145,158);
int hotend3 = color(20,255,10);
int hotend4 = color(108,22,237);
int hotend5 = color(255,0,0);
int hotend6 = color(0,0,255);
int[] colors = {hotend1, hotend2, hotend3, hotend4, hotend5, hotend6};


public void setup() {
  //set the window size
  
 
  //Draw the main screen with the graph
  textAlign(LEFT,BOTTOM);
  drawGraph();
  
  //print the available serial ports
  printArray(Serial.list());
  // Check the listed serial ports in your machine
  // and use the correct index number in Serial.list()[].
  SerialPort = new Serial(this, COMPORT, baudrate);  //
  
  // A serialEvent() is generated when a newline character is received :
  SerialPort.bufferUntil('\n');
}

public void draw() {
   //Drawing a line from Last temperature to the new one.  
   strokeWeight(1.10f);        //stroke wider
   int i;
   for (i=0; i<6; i++) 
   {
     if (temperatureList[i] <= 0)  //Hotend not connected. Negative Temperature
      {
      } else {
       stroke(colors[i]);
       line(lastxPos[i], lastheight[i], xPos[i], mappedTemperatures[i]); 
      }
   }
   noStroke();
   //Draw a white rectangle to delete the text
   fill(255);
   rect(525,625,100,20);
   fill(0);
   textSize(12);
   text("Time: " + millis()/1000.0f + " s",525,620,100,20); 
   
   printTemperaturesLegend();
}


//User Functions
public void setArray(int[] a , int v) {
    int i, n = a.length;
    for (i = 0; i < n; ++i) {
        a[i] = v;
    }
}

public void addToArray(int[] a, int v) {
    int i, n = a.length;
    for (i = 0; i < n; i++) {
       a[i] += v; 
    }
}

public void drawGraph() {
  int i;
  //set the rectangle for the graph
  background(253);
  rect(graphMargin,graphMargin,graphSpaceX, graphSpaceY);
  String s = "The Target Temperature is " + TARGETTEMPERATURE + "\u00baC";
  fill(50);
  text(s, 320, 15, 200, 20);  // Text wraps within text box
  line(40,134,760,134);  //Line at 250\u00baC
  
  //Draw the legend of colors at the bottom of the screen
  noStroke();
  textSize(12);
  for (i=0; i<6; i++) {
    fill(colors[i]);
    if(i<2) {
      rect(40,620+(i*30),20,20,20); 
      fill(0);
      text("hotend" + (i+1),70,635+(i*30));
    } else if (i<4) {
      rect(200,620+(i-2)*30,20,20,20);
      fill(0);
      text("hotend" + (i+1),230,635+((i-2)*30));
    } else if (i<6) {
      rect(360,620+(i-4)*30,20,20,20);
      fill(0);
      text("hotend" + (i+1),390,635+((i-4)*30));
    }
  }
  
  //back to stroke in black
  stroke(0);
  //Draw the Y axis marks for better display
  int j;
  int displayTemperature = 0;
  for (j=graphMargin; j<(graphSpaceY+graphMargin); j=j+PApplet.parseInt((graphSpaceY/axisDivisions)))
  {
    line(graphMargin-axisDivisionLenght, j, graphMargin + axisDivisionLenght, j);
    text(MAXTEMPERATURE-displayTemperature,graphMargin-axisDivisionLenght-25,j);
    displayTemperature += temperatureIncrementAxis;
  }

  //Draw the X axis marks for better display
  for (j=graphMargin; j<(graphSpaceX+graphMargin); j=j+PApplet.parseInt((graphSpaceX/axisDivisions)))
  {
    line(j,sizeY - graphMargin - axisDivisionLenght, j, sizeY - graphMargin + axisDivisionLenght);
  }
  
}

public void printTemperaturesLegend() {
  int i;
  int offset = 55;
  String displayString;
  noStroke();
  textSize(12);
    for (i=0; i<6; i++) {
      if (temperatureList[i] < 0.0f) {
        //Hotend not connected, or really really cold day
        displayString = "NC";
      } else {
        displayString = temperatureList[i] + " \u00baC"; 
      }
      fill(255);
      if(i<2) {
        rect(70+offset,620+(i*30),70,20); 
        fill(0);
        text(displayString,70+offset,635+(i*30));
      } else if (i<4) {
        rect(230+offset,620+(i-2)*30,70,20);
        fill(0);
        text(displayString,230+offset,635+((i-2)*30));
      } else if (i<6) {
        rect(390+offset,620+(i-4)*30,70,20);
        fill(0);
        text(displayString,390+offset,635+((i-4)*30));
      }
    }
}

public void interpolate() {
}

//This is where the magic happens
public void serialEvent (Serial SerialPort) {
  // get the ASCII string:
  inString = SerialPort.readStringUntil('\n');
  if (inString != null) {
    if (inString != "start") {
      //Separate strings by comas
      String[] temperatureStrings = split(inString, ',');
      
      //Convert the strings to floats
      int i;
      for (i=0; i<temperatureStrings.length; i++)
      {
        temperatureList[i] = PApplet.parseFloat(temperatureStrings[i]);
      }
      
      //map the values readed to the height of the screen
      for (i=0; i<temperatureList.length; i++)
      {
        if (temperatureList[i] <= 0)  //Hotend not connected. Negative Temperature
        {
          //Do nothing
        } else {                      //Map the temperature to the graph
          //Print the temperatures to the console. Used for debugging
          //print("Hotend " + (i+1) + ": ");
          //println(temperatureList[i]);
          mappedTemperatures[i] = PApplet.parseInt(map(temperatureList[i],0,MAXTEMPERATURE,graphSpaceY+graphMargin+10,graphMargin));
          //println(mappedTemperatures[i]);
          
          //Check if target temperature and calculate time
          if (temperatureList[i] >= TARGETTEMPERATURE) {
             //Temperature Reached 
             finishTime = millis();
             heatingTime = finishTime - startTime;
             println(heatingTime);
          }
        }
      }
  
      //Drawing a line from Last temperature to the new one.
      for (i=0; i<temperatureList.length; i++)
      {
      lastxPos[i] = xPos[i];
      lastheight[i] = PApplet.parseInt(mappedTemperatures[i]);
      }
  
      // at the edge of the window, go back to the beginning:
      if (xPos[0] >= sizeX - graphMargin) {
        setArray(xPos, graphMargin);
        setArray(lastxPos, graphMargin);
      } 
      else {
        // increment the horizontal position:
        addToArray(xPos,incrementXPosition);
      }
    } else {
      startTime = millis(); 
    }
  }
}
  public void settings() {  size(800,700); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TemperaturesPlotter" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}