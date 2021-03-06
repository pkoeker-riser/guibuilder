package de.guibuilder.framework;

import static javafx.concurrent.Worker.State.FAILED;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

public class GuiBrowserPanel extends GuiJFXPanel {
   private WebEngine engine;

   private JPanel panel = new JPanel(new BorderLayout());
   private JLabel lblStatus = new JLabel();

   private JButton btnGo = new JButton("Go");
   private JTextField txtURL = new JTextField();
   private JProgressBar progressBar = new JProgressBar();

   public GuiBrowserPanel(String url) {
      this.initComponents();
      if(url != null) {
         this.loadURL(url);
      }
   }

   private void initComponents() {
      createScene();

      ActionListener al = new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            loadURL(txtURL.getText());
         }
      };

      btnGo.addActionListener(al);
      txtURL.addActionListener(al);

      progressBar.setPreferredSize(new Dimension(150, 18));
      progressBar.setStringPainted(true);

      JPanel topBar = new JPanel(new BorderLayout(5, 0));
      topBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
      topBar.add(txtURL, BorderLayout.CENTER);
      topBar.add(btnGo, BorderLayout.EAST);

      JPanel statusBar = new JPanel(new BorderLayout(5, 0));
      statusBar.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
      statusBar.add(lblStatus, BorderLayout.CENTER);
      statusBar.add(progressBar, BorderLayout.EAST);

      panel.add(topBar, BorderLayout.NORTH);
      panel.add(jfxPanel, BorderLayout.CENTER);
      panel.add(statusBar, BorderLayout.SOUTH);

      //this.getJComponent().add(panel);
   }

   private void createScene() {

      Platform.runLater(new Runnable() {
         @Override
         public void run() {

            WebView view = new WebView();
            engine = view.getEngine();

            engine.titleProperty().addListener(new ChangeListener<String>() {
               @Override
               public void changed(ObservableValue<? extends String> observable, String oldValue, final String newValue) {
                  SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                        //##frame.setTitle(newValue);
                     }
                  });
               }
            });

            engine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
               @Override
               public void handle(final WebEvent<String> event) {
                  SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                        lblStatus.setText(event.getData());
                     }
                  });
               }
            });

            engine.locationProperty().addListener(new ChangeListener<String>() {
               @Override
               public void changed(ObservableValue<? extends String> ov, String oldValue, final String newValue) {
                  SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                        txtURL.setText(newValue);
                     }
                  });
               }
            });

            engine.getLoadWorker().workDoneProperty().addListener(new ChangeListener<Number>() {
               @Override
               public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, final Number newValue) {
                  SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run() {
                        progressBar.setValue(newValue.intValue());
                     }
                  });
               }
            });

            engine.getLoadWorker().exceptionProperty().addListener(new ChangeListener<Throwable>() {

               public void changed(ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) {
                  if(engine.getLoadWorker().getState() == FAILED) {
                     SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                           JOptionPane.showMessageDialog(panel, (value != null) ? engine.getLocation() + "\n" + value.getMessage() : engine.getLocation() + "\nUnexpected error.", "Loading error...", JOptionPane.ERROR_MESSAGE);
                        }
                     });
                  }
               }
            });

            jfxPanel.setScene(new Scene(view));
         }
      });
   }

   public void loadURL(final String url) {
      Platform.runLater(new Runnable() {
         @Override
         public void run() {
            String tmp = toURL(url);

            if(tmp == null) {
               tmp = toURL("http://" + url);
            }

            engine.load(tmp);
         }
      });
   }

   private static String toURL(String str) {
      try {
         return new URL(str).toExternalForm();
      }
      catch(MalformedURLException exception) {
         return null;
      }
   }

   public JComponent getJComponent() {
      return panel;
  }

  public WebEngine getEngine() {
     return this.engine;
  }

}