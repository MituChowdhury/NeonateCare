//Fahad Bin Mahbub
#include <SoftwareSerial.h>
#include <dht11.h>
#include <Servo.h>
#define RX 10
#define TX 11
#define dht_apin A0 // Analog Pin sensor is connected to
dht11 dht;
Servo myservo;
String AP = "Headquarter";       // AP NAME
String PASS = "Triple$:)"; // AP PASSWORD
String API = "MJ7U2LILF8CT8FNT";   // Write API KEY
String HOST = "api.thingspeak.com";
String PORT = "80";
int countTrueCommand;
int countTimeCommand; 
boolean found = false; 
int valSensor = 1;

int pos = 0,i;
// Motor B connections
int enB = 7;
int in3 = 9;
int in4 = 8;
int fan=0,swing=0;
float temperature,humidity;
  
SoftwareSerial esp8266(RX,TX); 
  
void setup() {
  Serial.begin(115200);
  esp8266.begin(115200);
  sendCommand("AT",5,"OK");
  sendCommand("AT+CWMODE=1",5,"OK");
  sendCommand("AT+CWJAP=\""+ AP +"\",\""+ PASS +"\"",20,"OK");

  myservo.attach(2);
  pinMode(enB, OUTPUT);
	pinMode(in3, OUTPUT);
	pinMode(in4, OUTPUT);
	
	digitalWrite(in3, LOW);
	digitalWrite(in4, LOW);
}

void checkFieldData(){
  String getFieldData = "GET /channels/2057774/fields/3/last.txt";
  sendCommand("AT+CIPMUX=1",5,"OK");
  sendCommand("AT+CIPSTART=0,\"TCP\",\""+ HOST +"\","+ PORT,15,"OK");
  sendCommand("AT+CIPSEND=0," +String(getFieldData.length()+4),4,">");
  esp8266.println(getFieldData);delay(1500);countTrueCommand++;
  sendCommand("AT+CIPCLOSE=0",5,"OK");

  String field3Data = esp8266.readStringUntil('\n');
  Serial.println("Field3 Data: " + field3Data);

  getFieldData = "GET /channels/2057774/fields/4/last.txt";
  sendCommand("AT+CIPMUX=1",5,"OK");
  sendCommand("AT+CIPSTART=0,\"TCP\",\""+ HOST +"\","+ PORT,15,"OK");
  sendCommand("AT+CIPSEND=0," +String(getFieldData.length()+4),4,">");
  esp8266.println(getFieldData);delay(1500);countTrueCommand++;
  sendCommand("AT+CIPCLOSE=0",5,"OK");

  String field4Data = esp8266.readStringUntil('\n');
  Serial.println("Field4 Data: " + field4Data);

  if (field3Data == "1") {
    digitalWrite(in3, LOW);
    digitalWrite(in4, HIGH);
    analogWrite(enB, 150);
  }
  else {
    digitalWrite(in3, LOW);
    digitalWrite(in4, LOW);
    analogWrite(enB, 0);
  }

  if (field4Data == "1") {
    myservo.write(180);
  }
  else {
    myservo.write(0);
  }
}

void loop() {
  
 String getData = "GET /update?api_key="+ API +"&field1="+getTemperatureValue()+"&field2="+getHumidityValue()+"&field3="+String(fan)+"&field4="+String(swing);
 sendCommand("AT+CIPMUX=1",5,"OK");
 sendCommand("AT+CIPSTART=0,\"TCP\",\""+ HOST +"\","+ PORT,15,"OK");
 sendCommand("AT+CIPSEND=0," +String(getData.length()+4),4,">");
 esp8266.println(getData);delay(1500);countTrueCommand++;
 sendCommand("AT+CIPCLOSE=0",5,"OK");
 checkFieldData();
 readDHTData();
 controlMotors();
}


void readDHTData() {
  dht.read(dht_apin);
  temperature = dht.temperature;
  Serial.print(" Temperature(C)= ");
  Serial.println(temperature);
  humidity = dht.humidity;
  Serial.print(" Humidity in %= ");
  Serial.println(humidity);
}

void controlMotors() {
  if(temperature>25 && humidity>=45)
  {
    fan=1;
    swing=1;    
    // Turn on motors
    digitalWrite(in3, LOW);
    digitalWrite(in4, HIGH);
	  delay(500);   
    for (pos = 0; pos <= 180; pos += 1) { 
      myservo.write(pos);
		  analogWrite(enB, 150);             
      delay(20);                      
    }
    for (pos = 180; pos >= 0; pos -= 1) { 
      myservo.write(pos);
  		analogWrite(enB, 150);              
      delay(15);                       
    }
	digitalWrite(in3, LOW);
	digitalWrite(in4, LOW);
  }
  else
  {
    fan=0;
    swing=1;
  }
}

String getTemperatureValue(){

   dht.read(dht_apin);
   Serial.print(" Temperature(C)= ");
   int temp = dht.temperature;
   Serial.println(temp); 
   delay(50);
   return String(temp); 
  
}


String getHumidityValue(){

   dht.read(dht_apin);
   Serial.print(" Humidity in %= ");
   int humidity = dht.humidity;
   Serial.println(humidity);
   delay(50);
   return String(humidity); 
  
}

void sendCommand(String command, int maxTime, char readReplay[]) {
  Serial.print(countTrueCommand);
  Serial.print(". at command => ");
  Serial.print(command);
  Serial.print(" ");
  while(countTimeCommand < (maxTime*1))
  {
    esp8266.println(command);//at+cipsend
    if(esp8266.find(readReplay))//ok
    {
      found = true;
      break;
    }
  
    countTimeCommand++;
  }
  
  if(found == true)
  {
    Serial.println("OYI");
    countTrueCommand++;
    countTimeCommand = 0;
  }
  
  if(found == false)
  {
    Serial.println("Fail");
    countTrueCommand = 0;
    countTimeCommand = 0;
  }
  
  found = false;
 }



