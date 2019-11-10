#include <WiFi.h>
#include <WiFiUdp.h>
#include <Ethernet.h>

#define UDP
//#define SERIAL

#define float_to_int(f) (*reinterpret_cast<const int*>(&static_cast<const float&>(f)))

#ifdef UDP
//wifi and ethernet settings
char ssid[] = "network";
char password[] = "pwd";
IPAddress ip(192, 168, 1, 177);
int port = 69420;
byte mac[] = {0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED};
int status = WL_IDLE_STATUS;
WiFiUDP udpHandler;
#endif

int okLED = -1;
int warnLED = -1;
int errLED = -1;

unsigned long timer = 0;

void setup() {
  pinMode(okLED, OUTPUT);
  pinMode(warnLED, OUTPUT);
  pinMode(errLED, OUTPUT);
  
  #ifdef SERIAL
  //usb
  Serial.begin(9600);
  while (!Serial);
  #endif
  
  #ifdef UDP
  //wifi
  if (WiFi.status() == WL_NO_SHIELD) {
    Serial.println("WiFi shield not present");
    // don't continue:
    while (true);
  }
  status = WiFi.begin(ssid, password);
  udpHandler.begin(port);
  #endif
}

void loop() {
    #ifdef SERIAL
    #endif

    #ifdef UDP
    int packetSize = udpHandler.parsePacket();
    //incoming packet
    if (packetSize > 0) {
      unsigned long interval = millis() - timer;
      connectionLED(interval);
      
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

void pack(byte dest[], float val) {
  int a = *(int*)&val;
  byte curr[4] = {(a >> 24) & 0xFF, (a >> 16) & 0xFF, (a >> 8) & 0xFF, (a)};
}
