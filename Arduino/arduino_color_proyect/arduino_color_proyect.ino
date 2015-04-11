/*
  Autor: Mario Pérez Esteso
  Web: www.geekytheory.com
  Twitter: @geekytheory
  Facebook: www.facebook.com/geekytheory
*/
const int LED_Rojo = 13;
const int LED_Amarillo=12;
int inByte = 0;
byte pines[4][3]={{'2','3','4'},{'5','6','7'},{'8','9','10'},{'11','12','13'}};
int pinesInt[4][3]={{2,3,4},{5,6,7},{8,9,10},{11,12,13}};
int filas = 4;
int cols = 3;
 
void setup(){
    Serial.begin(9600); //Open the serial port
    
    for (int fila = 0; fila < filas; fila++) {
          for (int col = 0; col < cols; col++) {
                pinMode(pinesInt[fila][col], OUTPUT);     
          }
    }      
    
     for (int fila = 0; fila < filas; fila++) {
          for (int col = 0; col < cols; col++) {
               digitalWrite(pinesInt[fila][col], LOW);     
          }
    } 
    
    /*pinMode(LED_Amarillo, OUTPUT);
    pinMode(LED_Rojo, OUTPUT);
    digitalWrite(LED_Amarillo, LOW);
    digitalWrite(LED_Rojo, LOW);*/
  }
 
void loop(){
 
    if(Serial.available() > 0){
 
        inByte = Serial.read();
        Serial.println(inByte);
        
        if(digitalRead(inByte)==HIGH){
                  digitalWrite(inByte, LOW);    
        }else{
      
          digitalWrite(inByte, HIGH);
        }
        
        
        /*if(inByte=='2'){
          digitalWrite(2, HIGH);
        }else if(inByte=='3'){
                  digitalWrite(3, HIGH);
        }
        else if(inByte=='4'){
                  digitalWrite(4, HIGH);
                }
        else if(inByte=='5'){
                  digitalWrite(5, HIGH);
        }
        else if(inByte=='6'){
                  digitalWrite(6, HIGH);
        }
        else if(inByte=='7'){
                    digitalWrite(7, HIGH);
        }
        else if(inByte=='8'){
                    digitalWrite(8, HIGH);
        }
        else if(inByte=='9'){
                    digitalWrite(9, HIGH);
        }
        else if(inByte=='10'){
                    digitalWrite(10, HIGH);
        }
        else if(inByte=='11'){
                    digitalWrite(11, HIGH);
        }
        else if(inByte=='12'){
                    digitalWrite(12, HIGH);
        }
        else if(inByte=='13'){
                    digitalWrite(13, HIGH);
        }*/
        
        //if(inByte=='0'){
          //controlLeds(false,0);
          //digitalWrite(13, LOW);     
        //}else{
          //controlLeds(true,inByte);
        //}
        
  //      digitalWrite(LED_Amarillo, HIGH);
        
/*        if(inByte == '0')
            digitalWrite(LED_Amarillo, LOW);
        else if(inByte=='1')
            digitalWrite(LED_Amarillo, HIGH);
        else if(inByte=='2')
            digitalWrite(LED_Rojo, LOW);
        else if(inByte=='3')
            digitalWrite(LED_Rojo, HIGH);*/
    }
    
   
}

void controlLeds(boolean encender,byte pin){
    boolean saltar=false;
     for (int fila = 0; fila < filas; fila++) {
          for (int col = 0; col < cols; col++) {
               if(!encender){
                   digitalWrite(pinesInt[fila][col], LOW);
               }else{
                 if(pines[fila][col]==pin){
                   saltar=true;
                   digitalWrite(pinesInt[fila][col], HIGH);
                   break;
                 }
                 
               }
          }
          if(saltar)
            break;
      }
}

