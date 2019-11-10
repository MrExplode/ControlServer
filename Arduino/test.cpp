// testing byte conversions
#include <iostream>
#include <string>

int floatToIntBits(float x) {
    
}

void pack(uint8_t dest[], float val) {
  int a = *(int*)&val;
  uint8_t curr[4] = {(a >> 24), (a >> 16), (a >> 8), (a)};
  std::cout << a << "    \n";
  std::cout << (a>>24) << " " <<((a >> 16) & 0xFF) << " " << ((a >> 8) & 0xFF) << " " << (a & 0xFF) << "\n";
  dest = curr;
}

int main()
{
  std::string name;
  std::cout << "What is your name? ";
  getline (std::cin, name);
  std::cout << "Hello, " << name << "!\n";
  
  uint8_t buf[4];
  pack(buf, 11.6f);
  for (int i = 0; i < sizeof(buf); i++) {
      printf("%d ", buf[i]);
  }
}
