package ru.ifmo.cs.bcomp.ui.components;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JLabel;
import ru.ifmo.cs.components.DataDestination;
import ru.ifmo.cs.components.Register;
import ru.ifmo.cs.components.Utils;

public class RegisterView extends BCompComponent implements DataDestination {
  private int formatWidth;
  
  private int valuemask;
  
  private boolean hex;
  
  private boolean isLeft;
  
  private final Register reg;
  
  protected final JLabel value = addValueLabel();
  
  public RegisterView(Register reg, Color colorTitleBG) {
    super("", 0, colorTitleBG);
    this.reg = reg;
  }
  
  public RegisterView(Register reg) {
    this(reg, DisplayStyles.COLOR_TITLE);
  }
  
  protected void setBounds(int x, int y, int wight) {
    setBounds(x, y, this.width = wight, this.height);
  }
  
  protected void setProperties(int x, int y, boolean hex, int regWidth, boolean isLeft) {
    this.hex = hex;
    this.formatWidth = regWidth;
    this.valuemask = (1 << regWidth) - 1;
    this.isLeft = isLeft;
    setBounds(x, y, getValueWidth(regWidth, hex) + 25 - 10);
    setValue();
    if (!isLeft) {
      this.title.setBounds(1, 1, 25, 26);
      this.value.setBounds(25, 1, this.width - 25 - 1, 26);
    } else {
      this.title.setBounds(this.width - 1 - 25, 1, 25, 26);
      this.value.setBounds(1, 1, this.width - 25 - 3, 26);
    } 
  }
  
  public void setProperties(int x, int y, boolean hex, boolean isLeft) {
    setProperties(x, y, hex, (int)this.reg.width, isLeft);
  }
  
  protected long getRegWidth() {
    return this.reg.width;
  }
  
  protected void setValue(String val) {
    this.value.setText(val);
  }
  
  public void setValue() {
    setValue(this.hex && !this.title.getText().equals("PS") ? 
        Utils.toHex(this.reg.getValue() & this.valuemask, this.formatWidth) : 
        Utils.toBinary(((int)this.reg.getValue() & this.valuemask), this.formatWidth));
  }
  
  void invertHex()
  {
	  this.hex = !hex;
  }
  
  public void setValue(long value) {
    setValue();
  }
  
  public Register getReg() {
    return this.reg;
  }
  
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.BLACK);
  }
}
