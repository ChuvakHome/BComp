package ru.ifmo.cs.bcomp.ui.components;

import static ru.ifmo.cs.bcomp.Reg.IR;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;

import ru.ifmo.cs.bcomp.BasicComp;
import ru.ifmo.cs.bcomp.CPU;
import ru.ifmo.cs.bcomp.ControlSignal;
import ru.ifmo.cs.bcomp.IOCtrl;
import ru.ifmo.cs.bcomp.Reg;
import ru.ifmo.cs.bcomp.SignalListener;
import ru.ifmo.cs.bcomp.State;
import ru.ifmo.cs.bcomp.translator.Translator;
import ru.ifmo.cs.bcomp.ui.GUI;
import ru.ifmo.cs.components.DataDestination;
import ru.ifmo.cs.components.Utils;

public class ComponentManager {
  private JRadioButton rbRanStop;
  
  private JRadioButton rbTact;
  
  private File prevDir = FileSystemView.getFileSystemView().getHomeDirectory();
  private boolean hex = false;
  
  private class SignalHandler implements DataDestination {
    private final ControlSignal signal;
    
    public SignalHandler(ControlSignal signal) {
      this.signal = signal;
    }
    
    public void setValue(long value) {
      ComponentManager.this.openBuses.add(this.signal);
    }
  }
  
  private class ButtonProperties {
    final String[] texts;
    
    public final ActionListener listener;
    
    public ButtonProperties(String[] texts, ActionListener listener) {
      this.texts = texts;
      this.listener = listener;
    }
  }
  
  private class ButtonsPanel extends JComponent {
    public ButtonsPanel() {
    	JFrame jFrame = (JFrame) JFrame.getFrames()[0];
		jFrame.setTitle(res.getString("title"));
		jFrame.addComponentListener(BCompWindowListener.INSTANCE);
    	Locale.setDefault(Locale.ENGLISH);
      setBounds(0, 530, DisplayStyles.PANE_WIDTH, 50);
      setLayout(new GridBagLayout());
      GridBagConstraints constraints = new GridBagConstraints() {
    	{
    	  	anchor = GridBagConstraints.WEST;
			fill = GridBagConstraints.HORIZONTAL;
			gridx = 0;
			gridy = 0;
			weightx = 1;
			insets = new Insets(1, 1, 1, 1);
        }
      };
      ComponentManager.this.buttons = new JButton[ComponentManager.this.buttonProperties.length];
      for (int i = 0; i < ComponentManager.this.buttons.length - 2; i++) {
        ComponentManager.this.buttons[i] = new JButton((ComponentManager.this.buttonProperties[i]).texts[0]);
        ComponentManager.this.buttons[i].setForeground(ComponentManager.this.buttonColors[0]);
        ComponentManager.this.buttons[i].setFont(DisplayStyles.FONT_COURIER_PLAIN_12);
        ComponentManager.this.buttons[i].setFocusable(false);
        ComponentManager.this.buttons[i].addActionListener((ComponentManager.this.buttonProperties[i]).listener);
        ComponentManager.this.buttons[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        constraints.gridwidth = (i == 0) ? 2 : 1;
        if (i > 0)
          constraints.gridy = 1; 
        if (i == 2)
          constraints.gridx = 0; 
        add(ComponentManager.this.buttons[i], constraints);
        if (i == 2) {
          constraints.gridx += 3;
        } else {
          constraints.gridx++;
        } 
      }
      
      constraints.gridy = 0;
      constraints.gridx = 3;
      constraints.fill = GridBagConstraints.NONE;
      constraints.anchor = GridBagConstraints.CENTER;
      ComponentManager.this.rbRanStop = new JRadioButton((ComponentManager.this.buttonProperties[5]).texts[0]);
      ComponentManager.this.rbRanStop.setFont(DisplayStyles.FONT_COURIER_PLAIN_12);
      ComponentManager.this.rbRanStop.setBackground(new Color(200, 221, 242));
      ComponentManager.this.rbRanStop.setBorderPainted(false);
      ComponentManager.this.rbRanStop.addActionListener((ComponentManager.this.buttonProperties[5]).listener);
      ComponentManager.this.rbRanStop.setCursor(new Cursor(12));
      ComponentManager.this.rbRanStop.setFocusPainted(false);
      ComponentManager.this.rbRanStop.setFocusable(false);
      add(ComponentManager.this.rbRanStop, constraints);
      constraints.gridx++;
      ComponentManager.this.rbTact = new JRadioButton((ComponentManager.this.buttonProperties[6]).texts[0]);
      ComponentManager.this.rbTact.setFont(DisplayStyles.FONT_COURIER_PLAIN_12);
      ComponentManager.this.rbTact.setBackground(new Color(200, 221, 242));
      ComponentManager.this.rbTact.setBorderPainted(false);
      ComponentManager.this.rbTact.addActionListener((ComponentManager.this.buttonProperties[6]).listener);
      ComponentManager.this.rbTact.setCursor(new Cursor(Cursor.HAND_CURSOR));
      ComponentManager.this.rbTact.setFocusPainted(false);
      ComponentManager.this.rbTact.setFocusable(false);
      add(ComponentManager.this.rbTact, constraints);
    }
  }
  
  private ResourceBundle res = ResourceBundle.getBundle("ru.ifmo.cs.bcomp.ui.components.loc", Locale.getDefault());
  
  private Color[] buttonColors = new Color[] { DisplayStyles.COLOR_TEXT, DisplayStyles.COLOR_ACTIVE };
  
  private ButtonProperties[] buttonProperties = new ButtonProperties[] { new ButtonProperties(new String[] { this.res
          .getString("setip") }, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ComponentManager.this.cmdEnterAddr();
          }
        }), new ButtonProperties(new String[] { this.res.getString("read") }, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ComponentManager.this.cmdRead();
          }
        }), new ButtonProperties(new String[] { this.res.getString("write") }, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ComponentManager.this.cmdWrite();
          }
        }), new ButtonProperties(new String[] { this.res.getString("start") }, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ComponentManager.this.cmdStart();
          }
        }), new ButtonProperties(new String[] { this.res.getString("continue") }, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ComponentManager.this.cmdContinue();
          }
        }), new ButtonProperties(new String[] { this.res.getString("stop"), this.res.getString("run") }, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ComponentManager.this.cmdInvertRunState();
          }
        }), new ButtonProperties(new String[] { this.res.getString("tick") }, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            ComponentManager.this.cmdInvertClockState();
          }
        }) };
  
  private final KeyAdapter keyListener = new KeyAdapter() {
      public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
          case 115:
            ComponentManager.this.cmdEnterAddr();
            break;
          case 116:
            ComponentManager.this.cmdWrite();
            break;
          case 117:
            ComponentManager.this.cmdRead();
            break;
          case 118:
            ComponentManager.this.cmdStart();
            break;
          case 119:
            ComponentManager.this.cmdContinue();
            break;
          case 120:
            if (e.isShiftDown()) {
              ComponentManager.this.cmdInvertClockState();
              break;
            } 
            ComponentManager.this.cmdInvertRunState();
            break;
          case 121:
            System.exit(0);
            break;
          case 122:
            ComponentManager.this.cmdPrevDelay();
            break;
          case 123:
            ComponentManager.this.cmdNextDelay();
            break;
          case KeyEvent.VK_H:
        	  if (e.isControlDown())
        	  {
//        		  for (int i = 0; i < 16; ++i)
//        		  {
//        			  String s = String.format("%04d", Integer.parseInt(Integer.toBinaryString(i)));
//        			  
//        			  System.out.println(s);
//        		  }
        		  
//        		  String s;
        		  
        		  Arrays.asList(Reg.values()).forEach(reg ->
        		  { 
        			  if (!(reg.equals(Reg.PS)))
        			  {
        				  String text = getRegisterView(reg).value.getText();
        				  
        				  if (hex)
        				  {
        					  String s;
        					  
        					  for (int i = 0; i < 16; ++i)
        	        		  {
        	        			  s = String.format("%04d", Integer.parseInt(Integer.toBinaryString(i)));
        	        			  text = text.replace(Integer.toHexString(i).toUpperCase(), " " + s);
        	        		  }
        					  
        					  text = text.trim();
        					  
        					  if (Boolean.parseBoolean(getRegisterView(reg).value.getName()))
        						  text = text.substring(1);
        				  
        					  getRegisterView(reg).setValue(text);
        				  }
        				  else
        				  {
        					  String s;
        					  
        					  for (int i = 0; i < 16; ++i)
        	        		  {
        	        			  s = String.format("%04d", Integer.parseInt(Integer.toBinaryString(i)));
        	        			  text = text.replace(s, Integer.toHexString(i).toUpperCase());
        	        		  }
        					  
        					  for (int i = 0; i < 8; ++i)
        	        		  {
        	        			  s = String.format("%03d", Integer.parseInt(Integer.toBinaryString(i)));
        	        			  text = text.replace(s, Integer.toHexString(i).toUpperCase());
        	        		  }
        					  
        					  text = text.replace(" ", "");
        					  
        					  getRegisterView(reg).value.setName(text.length() == 3 ? "true" : "false");
        					  getRegisterView(reg).setValue(text);
        				  }
        			  }
        			  
        			  getRegisterView(reg).invertHex();
        		  });
        		  
        		  hex = !hex;
        	  }
        	  
        	  input.invertHex();
        	  break;
          case KeyEvent.VK_M:
        	  if (e.isControlDown())
	        	  input.invertBinaryMode();
        	  break;
          case KeyEvent.VK_S:
        	  try
        	  {
        		  if (e.isControlDown())
        	  		ComponentManager.this.cmdLoadProgramm();
        	  } catch (Exception e2) {e2.printStackTrace();}
        	  break;
          case 81:
            if (e.isControlDown())
              System.exit(0); 
            break;
        } 
      }
    };
  
  private static final int BUTTON_RUN = 5;
  
  private static final int BUTTON_CLOCK = 6;
  
  private JButton[] buttons;
  
  private ButtonsPanel buttonsPanel = new ButtonsPanel();
  
  private final GUI gui;
  
  private final BasicComp bcomp;
  
  private final CPU cpu;
  
  private final IOCtrl[] ioctrls;
  
  private final MemoryView mem;
  
  private FlagView[] flagViews = new FlagView[4];
  
  private EnumMap<Reg, RegisterView> regs = new EnumMap<>(Reg.class);
  
  private InputRegisterView input;
  
  private ActiveBitView activeBit = new ActiveBitView(DisplayStyles.ACTIVE_BIT_X, 486);
  
  private volatile BCompPanel activePanel;
  
  private final long[] delayPeriods = new long[] { 0L, 1L, 5L, 10L, 25L, 50L, 100L, 1000L };
  
  private volatile int currentDelay = 3;
  
  private volatile int savedDelay;
  
  private final Object lockActivePanel = new Object();
  
  private volatile boolean cuswitch = false;
  
  private final SignalListener[] listeners;
  
  private ArrayList<ControlSignal> openBuses = new ArrayList<>();
  
  private static final ControlSignal[] busSignals = new ControlSignal[] { 
      ControlSignal.RDDR, ControlSignal.RDCR, ControlSignal.RDIP, ControlSignal.RDAC, ControlSignal.RDPS, ControlSignal.RDIR, ControlSignal.RDBR, ControlSignal.RDSP, ControlSignal.WRDR, ControlSignal.WRCR, 
      ControlSignal.WRIP, ControlSignal.WRAC, ControlSignal.WRPS, ControlSignal.WRAR, ControlSignal.WRBR, ControlSignal.WRSP, ControlSignal.LOAD, ControlSignal.STOR, ControlSignal.IO, ControlSignal.TYPE };
  
  public ComponentManager(GUI gui) {
    this.gui = gui;
    this.bcomp = gui.getBasicComp();
    this.cpu = gui.getCPU();
    this.input = new InputRegisterView(this, this.cpu.getRegister(Reg.IR)) {
        protected void setValue(String val) {
          super.setValue(val);
          ComponentManager.this.getRegisterView(Reg.IR).setValue(Utils.toBinary(ComponentManager.this.cpu.getRegister(Reg.IR).getValue(), (int)ComponentManager.this.input.getRegWidth()));
//          getRegisterView(IR).setValue(String.format("%04X", cpu.getRegister(IR).getValue()));
          if (ComponentManager.this.hex)
        	  ComponentManager.this.getRegisterView(IR).setValue(String.format("%04X", cpu.getRegister(IR).getValue()));
          else
        	  ComponentManager.this.getRegisterView(Reg.IR).setValue(Utils.toBinary(ComponentManager.this.cpu.getRegister(Reg.IR).getValue(), (int)ComponentManager.this.input.getRegWidth()));
        }
      };
    this.ioctrls = gui.getIOCtrls();
    this.cpu.setTickStartListener(new Runnable() {
          public void run() {
            synchronized (ComponentManager.this.lockActivePanel) {
              if (ComponentManager.this.activePanel != null)
                ComponentManager.this.activePanel.stepStart(); 
            } 
            ComponentManager.this.openBuses.clear();
          }
        });
    this.cpu.setTickFinishListener(new Runnable() {
          public void run() {
            synchronized (ComponentManager.this.lockActivePanel) {
              if (ComponentManager.this.activePanel != null)
                ComponentManager.this.activePanel.stepFinish(); 
            } 
            if (ComponentManager.this.delayPeriods[ComponentManager.this.currentDelay] != 0L)
              try {
                Thread.sleep(ComponentManager.this.delayPeriods[ComponentManager.this.currentDelay]);
              } catch (InterruptedException interruptedException) {} 
          }
        });
    for (ControlSignal cs : busSignals)
      this.cpu.addDestination(cs, new SignalHandler(cs)); 
    for (int i = 0; i < 4; i++) {
      this.flagViews[i] = new FlagView(0, 0, 25, 25);
      this.flagViews[i].setPreferredSize(this.flagViews[i].getSize());
    } 
    this.flagViews[0].setTitle("N");
    this.flagViews[1].setTitle("Z");
    this.flagViews[2].setTitle("V");
    this.flagViews[3].setTitle("C");
    for (Reg reg : Reg.values())
      this.regs.put(reg, new RegisterView(this.cpu.getRegister(reg))); 
    this.listeners = new SignalListener[] { new SignalListener(this.regs.get(Reg.AR), new ControlSignal[] { ControlSignal.WRAR }), new SignalListener(this.regs.get(Reg.DR), new ControlSignal[] { ControlSignal.WRDR, ControlSignal.LOAD }), new SignalListener(this.regs.get(Reg.CR), new ControlSignal[] { ControlSignal.WRCR, ControlSignal.IRQS }), new SignalListener(this.regs.get(Reg.IP), new ControlSignal[] { ControlSignal.WRIP }), new SignalListener(this.regs.get(Reg.AC), new ControlSignal[] { ControlSignal.WRAC, ControlSignal.IO }), new SignalListener(this.regs.get(Reg.PS), new ControlSignal[] { ControlSignal.RDPS, ControlSignal.WRPS, ControlSignal.SETC, ControlSignal.SETV, ControlSignal.STNZ, ControlSignal.SET_EI, ControlSignal.HALT, ControlSignal.SET_PROGRAM }), new SignalListener(this.regs.get(Reg.SP), new ControlSignal[] { ControlSignal.WRSP }), new SignalListener(this.regs.get(Reg.BR), new ControlSignal[] { ControlSignal.WRBR }) };
    this.mem = new MemoryView(this.cpu.getMemory(), 1, 1);
    this.cpu.addDestination(ControlSignal.LOAD, new DataDestination() {
          public void setValue(long value) {
            if (ComponentManager.this.activePanel != null) {
              ComponentManager.this.mem.eventRead();
            } else {
              ComponentManager.this.mem.updateLastAddr();
            } 
          }
        });
    this.cpu.addDestination(ControlSignal.SETC, new DataDestination() {
          public void setValue(long value) {
            ComponentManager.this.flagViews[3].setActive((ComponentManager.this.cpu.getProgramState(State.C) == 1L));
          }
        });
    this.cpu.addDestination(ControlSignal.SETV, new DataDestination() {
          public void setValue(long value) {
            ComponentManager.this.flagViews[2].setActive((ComponentManager.this.cpu.getProgramState(State.V) == 1L));
          }
        });
    this.cpu.addDestination(ControlSignal.STNZ, new DataDestination() {
          public void setValue(long value) {
            ComponentManager.this.flagViews[1].setActive((ComponentManager.this.cpu.getProgramState(State.Z) != 0L));
            ComponentManager.this.flagViews[0].setActive((ComponentManager.this.cpu.getProgramState(State.N) != 0L));
          }
        });
    this.cpu.addDestination(ControlSignal.STOR, new DataDestination() {
          public void setValue(long value) {
            if (ComponentManager.this.activePanel != null) {
              ComponentManager.this.mem.eventWrite();
            } else {
              ComponentManager.this.mem.updateLastAddr();
            } 
          }
        });
  }
  
  public void panelActivate(BCompPanel component) {
    synchronized (this.lockActivePanel) {
      this.activePanel = component;
      this.bcomp.addDestination(this.listeners);
      this.bcomp.addDestination(this.activePanel.getSignalListeners());
    } 
    this.buttonsPanel.setPreferredSize(this.buttonsPanel.getSize());
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.anchor = 10;
    constraints.fill = 0;
    constraints.gridx = 2;
    constraints.gridy = 0;
    constraints.gridheight = 2;
    constraints.insets = new Insets(0, 0, 0, 30);
    this.input.setProperties(0, 0, false, true);
    this.input.setTitle("IR");
    this.input.setPreferredSize(this.input.getSize());
    this.input.setMinimumSize(this.input.getSize());
    this.buttonsPanel.add(this.input, constraints);
    constraints.anchor = 10;
    constraints.insets = new Insets(0, DisplayStyles.REG_16_WIDTH + 26, 0, 20);
    this.activeBit.setPreferredSize(this.activeBit.getSize());
    this.activeBit.setMinimumSize(this.activeBit.getSize());
    this.buttonsPanel.add(this.activeBit, constraints);
    this.mem.setPreferredSize(this.mem.getSize());
    component.add(this.buttonsPanel, "South");
    this.mem.updateMemory();
    this.cuswitch = false;
    switchFocus();
  }
  
  public void panelDeactivate() {
    synchronized (this.lockActivePanel) {
      this.bcomp.removeDestination(this.listeners);
      this.bcomp.removeDestination(this.activePanel.getSignalListeners());
      this.activePanel = null;
    } 
  }
  
  public void keyPressed(KeyEvent e) {
    this.keyListener.keyPressed(e);
  }
  
  public void switchFocus() {
    this.input.setActive();
  }
  
  public RegisterView getRegisterView(Reg reg) {
    return this.regs.get(reg);
  }
  
  public FlagView getFlagView(int i) {
    return this.flagViews[i];
  }
  
  public void cmdContinue() {
    this.cpu.startContinue();
  }
  
  public void cmdEnterAddr() {
    this.cpu.startSetAddr();
  }
  
  public void cmdWrite() {
    this.cpu.startWrite();
  }
  
  public void cmdRead() {
    this.cpu.startRead();
  }
  
  public void cmdStart() {
    this.cpu.startStart();
  }
  
  public void cmdInvertRunState() {
    this.cpu.invertRunState();
    long state = this.cpu.getProgramState(State.W);
    this.rbRanStop.setSelected((state == 1L));
    this.rbRanStop.setText((this.buttonProperties[5]).texts[(int)state]);
    ((RegisterView)this.regs.get(Reg.PS)).setValue();
  }
  
  public void cmdInvertClockState() {
    boolean state = this.cpu.invertClockState();
    this.rbTact.setSelected(!state);
  }
  
  public void cmdNextDelay() {
    this.currentDelay = (this.currentDelay < this.delayPeriods.length - 1) ? (this.currentDelay + 1) : 0;
  }
  
  public void cmdPrevDelay() {
    this.currentDelay = ((this.currentDelay > 0) ? this.currentDelay : this.delayPeriods.length) - 1;
  }
  
  public void cmdLoadProgramm() throws IOException 
  {
		JFileChooser jfc = new JFileChooser(prevDir);
		jfc.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT file programm", "txt");
		jfc.addChoosableFileFilter(filter);
		
		JRadioButton jRadioButton = new JRadioButton(res.getString("singleFile"));
		jRadioButton.setSelected(false);
		jRadioButton.addItemListener(e -> 
		{
			jfc.setMultiSelectionEnabled(jRadioButton.isSelected());
			jRadioButton.setText(res.getString(jRadioButton.isSelected() ? "multipleFiles" : "singleFile"));
		});
		
		jfc.add(jRadioButton);
		
		int returnValue = jfc.showOpenDialog(null);
		
		if (returnValue == JFileChooser.APPROVE_OPTION) 
		{
			prevDir = jfc.getCurrentDirectory();
			
			File[] selected = {jfc.getSelectedFile()};
			
			if (jfc.isMultiSelectionEnabled())
				selected = jfc.getSelectedFiles();
			
			for (File f: selected)
			{	
				Scanner file = new Scanner(f);
				
				while (file.hasNext()) {
					String line = file.nextLine().trim();
					
					if (!line.isEmpty()) {
						if (line.substring(line.length() - 1).equals("a")) {
							String addr = line.replaceFirst(".$", "");

							Integer value = Integer.parseInt(addr, 16);
							cpu.getRegister(Reg.IR).setValue(value);
							cpu.executeSetAddr();
						} else {
							String code = Translator.translate(line);
							Integer value = Integer.parseInt(code, 16);
							cpu.getRegister(Reg.IR).setValue(value);
							cpu.executeWrite();
						}
					}
				}

				file.close();
			}
		}
	}
  
  public void saveDelay() {
    this.savedDelay = this.currentDelay;
    this.currentDelay = 0;
  }
  
  public void restoreDelay() {
    this.currentDelay = this.savedDelay;
  }
  
  public ActiveBitView getActiveBit() {
    return this.activeBit;
  }
  
  public KeyListener getKeyListener() {
    return this.keyListener;
  }
  
  public ArrayList<ControlSignal> getActiveSignals() {
    return this.openBuses;
  }
  
  public void clearActiveSignals() {
    this.openBuses.clear();
  }
  
  public MemoryView getMem() {
    return this.mem;
  }
  
  public ResourceBundle getRes() {
    return this.res;
  }
}
