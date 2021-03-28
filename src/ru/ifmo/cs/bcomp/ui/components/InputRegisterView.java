package ru.ifmo.cs.bcomp.ui.components;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import ru.ifmo.cs.components.Register;
import ru.ifmo.cs.components.Utils;

public class InputRegisterView extends RegisterView {
  private final ComponentManager cmanager;
  
  private final Register reg;
  
  private final ActiveBitView activeBitView;
  
  private boolean active = false;
  
  private int regWidth;
  
  private int bitno;
  
  private int formattedWidth;
  
  private boolean binaryMode = false;
  
  public InputRegisterView(ComponentManager cmgr, Register reg) {
    super(reg, DisplayStyles.COLOR_TITLE);
    this.cmanager = cmgr;
    this.reg = reg;
    this.activeBitView = this.cmanager.getActiveBit();
    this.bitno = (this.regWidth = (int)reg.width) - 1;
    this.formattedWidth = Utils.getBinaryWidth(this.regWidth);
    addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (!InputRegisterView.this.value.isFocusOwner())
              InputRegisterView.this.reqFocus(); 
          }
        });
    this.value.setFocusable(true);
    this.value.addFocusListener(new FocusListener() {
          public void focusGained(FocusEvent e) {
            InputRegisterView.this.active = true;
            InputRegisterView.this.setActiveBit(InputRegisterView.this.bitno);
          }
          
          public void focusLost(FocusEvent e) {
            InputRegisterView.this.active = false;
            InputRegisterView.this.setValue();
          }
        });
    this.value.addKeyListener(new KeyAdapter() {
          public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
              case KeyEvent.VK_BACK_SPACE:
              case KeyEvent.VK_LEFT:
            	InputRegisterView.this.moveLeft();
                return;
              case KeyEvent.VK_RIGHT:
            	InputRegisterView.this.moveRight();
                return;
              case 38:
                InputRegisterView.this.invertBit();
                return;
              case KeyEvent.VK_0:
              case KeyEvent.VK_1:
            	  if (binaryMode)
            	  {
            		  InputRegisterView.this.setBit(e.getKeyCode() - KeyEvent.VK_0);
            		  return;
            	  }
              case KeyEvent.VK_2:
              case KeyEvent.VK_3:
              case KeyEvent.VK_4:
              case KeyEvent.VK_5:
              case KeyEvent.VK_6:
              case KeyEvent.VK_7:
              case KeyEvent.VK_8:
              case KeyEvent.VK_9:
            	  if (!binaryMode)
	            	  InputRegisterView.this.setHexDigit(e.getKeyCode() - KeyEvent.VK_0);
	                  
            	  return;
              case KeyEvent.VK_NUMPAD0:
              case KeyEvent.VK_NUMPAD1:
            	  if (binaryMode)
            	  {
            		  InputRegisterView.this.setBit(e.getKeyCode() - KeyEvent.VK_0);
            		  return;
            	  }
              case KeyEvent.VK_NUMPAD2:
              case KeyEvent.VK_NUMPAD3:
              case KeyEvent.VK_NUMPAD4:
              case KeyEvent.VK_NUMPAD5:
              case KeyEvent.VK_NUMPAD6:
              case KeyEvent.VK_NUMPAD7:
              case KeyEvent.VK_NUMPAD8:
              case KeyEvent.VK_NUMPAD9:
            	  if (!binaryMode)
	            	  InputRegisterView.this.setHexDigit(e.getKeyCode() - KeyEvent.VK_NUMPAD0);
	                  
            	  return;
              case KeyEvent.VK_A:
              case KeyEvent.VK_B:
              case KeyEvent.VK_C:
              case KeyEvent.VK_D:
              case KeyEvent.VK_E:
              case KeyEvent.VK_F:
            	  if (!binaryMode)
            		  InputRegisterView.this.setHexDigit(0xA + e.getKeyCode() - KeyEvent.VK_A);
            		
            	  return;
            } 
            InputRegisterView.this.cmanager.keyPressed(e);
          }
        });
    this.value.addMouseListener(new MouseAdapter() {
          public void mouseClicked(MouseEvent e) {
            if (!InputRegisterView.this.value.isFocusOwner())
              InputRegisterView.this.reqFocus(); 
            int bitno = Utils.getBitNo(e.getX(), (e.getX() > InputRegisterView.this.value.getWidth() / 2) ? (InputRegisterView.this.formattedWidth - 1) : InputRegisterView.this.formattedWidth, DisplayStyles.FONT_COURIER_BOLD_21_WIDTH);
            if (bitno < 0)
              return; 
            InputRegisterView.this.setActiveBit(bitno);
            if (e.getClickCount() > 1)
              InputRegisterView.this.invertBit(); 
          }
        });
  }
  
  private void setActiveBit(int bitno) {
    this.activeBitView.setValue(this.bitno = bitno);
    setValue();
  }
  
  private void moveLeft() {
	  if (binaryMode)
		  setActiveBit((this.bitno + 1) % this.regWidth);
	  else
		  moveLeftHexDigit();
  }
  
  private void moveRight() {
	  if (binaryMode)
		  setActiveBit(((this.bitno == 0) ? this.regWidth : this.bitno) - 1);
	  else
		  moveRightHexDigit();
  }
  
  private void invertBit() {
    this.reg.invertBit(this.bitno);
    setValue();
  }
  
  void invertBinaryMode()
  {
	  this.binaryMode = !binaryMode;
  }
  
  private void setBit(int value)
  {
	  this.reg.setValue(value, 1L, this.bitno);
  		moveRight();
  }
  
  private void setHexDigit(int value) 
  {
    int digit;
    
    if (this.bitno >= 12)
    	digit = 12;
    else if (this.bitno >= 8)
    	digit = 8;
    else if (this.bitno >= 4)
    	digit = 4;
    else
    	digit = 0;
    
    for (int i = 0; i < 4; ++i)
    {
    	this.reg.setValue(value % 2, 1L, digit + i);
    	
    	value >>= 1;
    }
    
    digit /= 4;
    
    moveRight();
  }
  
  private void moveLeftHexDigit()
  {
	  int digit;
	    
	    if (this.bitno >= 12)
	    	digit = 3;
	    else if (this.bitno >= 8)
	    	digit = 2;
	    else if (this.bitno >= 4)
	    	digit = 1;
	    else
	    	digit = 0;
	    
	    setActiveBit((digit + 1) % 4 * 4 + 3);
  }
  
  private void moveRightHexDigit()
  {
	  int digit;
	    
	    if (this.bitno >= 12)
	    	digit = 3;
	    else if (this.bitno >= 8)
	    	digit = 2;
	    else if (this.bitno >= 4)
	    	digit = 1;
	    else
	    	digit = 0;
	    
	    setActiveBit((digit == 0 ? digit = 3 : digit - 1) * 4 + 3);
  }
  
  public void setValue() {
    if (this.active) {
      StringBuilder str = new StringBuilder("<html>" + Utils.toBinary((int)this.reg.getValue(), this.regWidth) + "</html>");
      int pos = 6 + this.formattedWidth - Utils.getBinaryWidth(this.bitno + 1);
      str.insert(pos + 1, "</font>");
      str.insert(pos, "<font color=\"#FF0000\">");
      setValue(str.toString());
    } else {
      setValue("<html>" + Utils.toBinary(this.reg.getValue(), this.regWidth) + "</html>");
    } 
  }
  
  public void reqFocus() {
    try {
      this.value.requestFocus();
    } catch (Exception exception) {}
    this.value.requestFocusInWindow();
  }
  
  public void setActive() {
    reqFocus();
    this.active = true;
    setActiveBit(this.bitno);
  }
}
