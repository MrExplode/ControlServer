#include <WiFi.h>
#include <WiFiUdp.h>
#include <Ethernet.h>

union floatData {
  float value;
  byte bytes[4];
};

//############################
// Mode switches
// Comment out to toggle them
#define UDP
//#define SERIAL
#define SENSORS
//############################

#ifdef UDP
//wifi and ethernet settings
char ssid[] = "network";
char password[] = "pwd";
IPAddress ip(192, 168, 1, 177);
int port = 42069;
byte mac[] = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED};
int status = WL_IDLE_STATUS;
WiFiUDP udpHandler;
#endif

byte buffer[64];

const int okLED = -1;
const int warnLED = -1;
const int errLED = -1;

#ifdef SENSORS
//minimum values for obstacle avoiding
const float minFrontDist = 10.0f;
const float minLeftDist = 10.0f;
const float minRightDist = 10.0f;

//Center ultrasonic sensor
const int uss1Trig = -1;
const int uss1Echo = -1;
//Left ultrasonic sensor
const int uss2Trig = -1;
const int uss2Echo = -1;
//right ultrasonic sensor
const int uss3Trig = -1;
const int uss3Echo = -1;
#endif

unsigned long timer = 0;

void setup() {
  setPinModes();
  #ifdef SERIAL
  //usb
  Serial.begin(9600);
  while (!Serial);
  #endif
  
  #ifdef UDP
  //wifi
  if (WiFi.status() == WL_NO_SHIELD) {
    //questionable
    Serial.println("WiFi shield not present");
    // don't continue:
    while (true);
  }
  status = WiFi.begin(ssid, password);
  udpHandler.begin(port);
  #endif
}

void loop() {
    float turn = 0.0f;
    float throttle = 0.0f;
    #ifdef SERIAL
    #endif

    #ifdef UDP
    int packetSize = udpHandler.parsePacket();
    //incoming packet
    if (packetSize > 0) {
      unsigned long interval = millis() - timer;
      connectionLED(interval);
      //TODO other stuff like checks
      udpHandler.read(buffer, sizeof(buffer));

      floatData turnData;
      memcpy(turnData.bytes, buffer, 4);
      floatData throttleData;
      memcpy(throttleData.bytes, buffer + 4, 4);
      turn = turnData.value;
      throttle = throttleData.value;
    }
    #endif

    #ifdef SENSORS
      //polling ultrasonic sensors   referece -> https://howtomechatronics.com/tutorials/arduino/ultrasonic-sensor-hc-sr04/
      digitalWrite(uss1Trig, LOW);
      digitalWrite(uss2Trig, LOW);
      digitalWrite(uss3Trig, LOW);
      delayMicroseconds(2);

      digitalWrite(uss1Trig, HIGH);
      digitalWrite(uss2Trig, HIGH);
      digitalWrite(uss3Trig, HIGH);
      delayMicroseconds(10);
      digitalWrite(uss1Trig, LOW);
      digitalWrite(uss2Trig, LOW);
      digitalWrite(uss3Trig, LOW);

      float frontDistance = pulseIn(uss1Echo, HIGH)*0.034/2;
      float leftDistance = pulseIn(uss2Echo, HIGH)*0.34/2;
      float rightDistance = pulseIn(uss3Echo, HIGH)*0.34/2;

      if (frontDistance <= minFrontDist) {
        //stop
        throttle = 0.0f;
      }

      if (leftDistance <= minLeftDist) {
        //right turn
        turn = 1.0f;
      }

      if (rightDistance <= minRightDist) {
        turn = -1.0f;
      }
      #endif
}

void connectionLED(unsigned long interval) {
  if (interval <= 60) {
    digitalWrite(okLED, HIGH);
    digitalWrite(warnLED, LOW);
    digitalWrite(errLED, LOW);
  } else if (interval <= 70) {
    digitalWrite(okLED, LOW);
    digitalWrite(warnLED, HIGH);
    digitalWrite(errLED, LOW);
  } else {
    digitalWrite(okLED, LOW);
    digitalWrite(warnLED, LOW);
    digitalWrite(errLED, HIGH);
  }
}

void setPinModes() {
  pinMode(okLED, OUTPUT);
  pinMode(warnLED, OUTPUT);
  pinMode(errLED, OUTPUT);

  #ifdef SENSORS
  pinMode(uss1Trig, OUTPUT);
  pinMode(uss2Trig, OUTPUT);
  pinMode(uss3Trig, OUTPUT);
  
  pinMode(uss1Echo, INPUT);
  pinMode(uss2Echo, INPUT);
  pinMode(uss3Echo, INPUT);
  #endif
}
