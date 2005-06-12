/*
 * damnWhois.java
 *
 * Created on June 12, 2005, 8:49 AM
 */

package client;

/**
 *
 * @author  mikl
 */


/* Thread waits for the user information, and then update the whois gui */
class waitForInfoThread extends Thread
{
    private damnProtocol dp;
    private damnWhois dw;
    
    public waitForInfoThread(damnProtocol _dp, damnWhois _dw)
    {
        dp = _dp;
        dw = _dw;
    }
    
    public void run()
    {
        int cntr = 0;            
        while (cntr++ < 20 && !dp.whoisInfoReady && !dp.whoisBadUsername)
        {
            try {
                sleep(100);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
        
        if (dp.whoisInfoReady) {
            dw.updateGui( dp.whoisData() );
        } else dw.updateGui( null );
    }
}


/* The whois GUI class for damnChat */
public class damnWhois extends javax.swing.JFrame {
    
    private damnProtocol dP;
    private String userName;
    /** Creates new form damnWhois */
    
    public damnWhois(damnProtocol _dP) {
        dP = _dP;
        initComponents();
    }
    
    /** This method is used to show the user information window to a given user
     *  If user doesn't exist, --- are shown in each textboxes 
     */
    public void showWhois(String userName)
    {
        this.userName = userName;
        jLabel1.setText(userName);
        jTextField6.setText("");
        updateGui(null);
        show();
        dP.doGetUserInformation(userName);
        new waitForInfoThread(dP, this).start();
    }
    
    /** This method decodes the raw userinfo data to visible form
     * @param data The raw String array containing the userinfo data
     */
    public void updateGui(String[] data)
    {
        if (data != null) {
            
            String iconString="0";
   
            for (int i=0; i<11; i++) {
                String[] d = data[i].split("=");
                if (d[0].equals("realname")) jTextField1.setText(d[1]);
                if (d[0].equals("typename")) jTextField2.setText(d[1]);
                if (d[0].equals("gpc")) jTextField3.setText(d[1]);
                if (d[0].equals("online")) jTextField4.setText( String.valueOf( Integer.parseInt(d[1])/60) + " secs");
                if (d[0].equals("idle")) jTextField5.setText( String.valueOf( Integer.parseInt(d[1])/60) + " secs" );
                if (d[0].equals("usericon")) iconString = d[1];  
            }

            String[] types = {"","gif","jpg","png"};
            int type = Integer.parseInt(iconString);
            String name = userName.toLowerCase();
            String avatar = "<img src=\"http://a.deviantart.com/avatars/"+name.charAt(0)+"/"+name.charAt(1)+"/"+name+"."+types[type]+"\">";
            if (type == 0)
                avatar = "<img src=\"http://a.deviantart.com/avatars/default.gif\">";

            jAvatarPane.setText("<HTML><BODY>"+avatar+"</BODY></HTML>");
            
        } else {
            jTextField1.setText("---");
            jTextField2.setText("---");
            jTextField3.setText("---");
            jTextField4.setText("---");
            jTextField5.setText("---");
            String avatar = "<img src=\"http://a.deviantart.com/avatars/default.gif\">";
            jAvatarPane.setContentType("text/html");
            jAvatarPane.setText("<HTML><BODY>"+avatar+"</BODY></HTML>");
            
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jAvatarPane = new javax.swing.JEditorPane();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jTextField6 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jButton2 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("User Information");
        setResizable(false);
        jPanel1.setLayout(new java.awt.GridLayout(6, 2));

        jPanel1.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        jAvatarPane.setEditable(false);
        jAvatarPane.setEnabled(false);
        jAvatarPane.setFocusable(false);
        jAvatarPane.setPreferredSize(new java.awt.Dimension(50, 50));
        jPanel4.add(jAvatarPane);

        jPanel1.add(jPanel4);

        jLabel1.setText("<nick>");
        jPanel1.add(jLabel1);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Real name:");
        jLabel2.setMaximumSize(new java.awt.Dimension(30, 15));
        jLabel2.setMinimumSize(new java.awt.Dimension(30, 15));
        jLabel2.setPreferredSize(new java.awt.Dimension(30, 15));
        jPanel1.add(jLabel2);

        jTextField1.setEditable(false);
        jTextField1.setText("---");
        jTextField1.setPreferredSize(new java.awt.Dimension(200, 19));
        jPanel1.add(jTextField1);

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Description:");
        jPanel1.add(jLabel3);

        jTextField2.setEditable(false);
        jTextField2.setText("---");
        jPanel1.add(jTextField2);

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Gpc :");
        jPanel1.add(jLabel4);

        jTextField3.setEditable(false);
        jTextField3.setText("---");
        jPanel1.add(jTextField3);

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Online:");
        jPanel1.add(jLabel5);

        jTextField4.setEditable(false);
        jTextField4.setText("---");
        jPanel1.add(jTextField4);

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel6.setText("Idle:");
        jPanel1.add(jLabel6);

        jTextField5.setEditable(false);
        jTextField5.setText("---");
        jPanel1.add(jTextField5);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout());

        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });

        jPanel2.add(jTextField6);

        jButton1.setText("Get");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel2.add(jButton1);

        getContentPane().add(jPanel2, java.awt.BorderLayout.NORTH);

        jButton2.setText("Ok");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jPanel3.add(jButton2);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
        userName = jTextField6.getText();
        jLabel1.setText(userName);
        dP.doGetUserInformation(userName);
        new waitForInfoThread(dP, this).start();
        
    }//GEN-LAST:event_jTextField6ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        userName = jTextField6.getText();
        jLabel1.setText(userName);
        dP.doGetUserInformation(userName);
        new waitForInfoThread(dP, this).start();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JEditorPane jAvatarPane;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    // End of variables declaration//GEN-END:variables
    
}
