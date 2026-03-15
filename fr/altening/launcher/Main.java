package fr.altening.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

import static fr.altening.launcher.Utils.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import fr.litarvan.openauth.microsoft.MicrosoftAuthResult;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;

@SuppressWarnings("serial")
public class Main extends JFrame{
	public Auth auth;
    public static Main main;
    public JButton launchButton;
    public JButton btnAction;

	public Main() {
		setTitle("Minecraft Launcher");
        setSize(500, 250);
        setLocationRelativeTo(null); // Centrer la fenêtre
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        /*try {
        	setIconImage(ImageIO.read(Main.class.getResource("/assets/icon32.png")).getScaledInstance(32, 32, 0));
        } catch (IOException e) {
        	e.printStackTrace();
        }*/
        
        // Fond avec panneau personnalis\u00e9
        JPanel mainPanel = new JPanel();	
        mainPanel.setBackground(new Color(34, 34, 34));
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setContentPane(mainPanel);

        // Texte d'accueil
        JLabel text = new JLabel("Launcher Minecraft (<1.16.5)", SwingConstants.CENTER);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 16));
        text.setBorder(null);
        text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(34, 34, 34));
        topBar.add(text, BorderLayout.CENTER);
        mainPanel.add(topBar, BorderLayout.PAGE_START);

        JLabel logoLabel = new JLabel("Minecraft Launcher");
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(logoLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(34, 34, 34));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel nameLinePanel = new JPanel();
        nameLinePanel.setLayout(new BoxLayout(nameLinePanel, BoxLayout.X_AXIS));
        nameLinePanel.setBackground(new Color(34, 34, 34));

        JComboBox<String> versionCombo = new JComboBox<String>();
        SwingUtilities.invokeLater(() -> {
        	Utils.getVersionData(false, false, false);//Add versions from versionManifest
        	Utils.getVersionsFromFolder();//add version or filter if already exists
        	for (VersionData versions : Utils.versionList) {
        		versionCombo.addItem(versions.getType() + " " + versions.getId());//Add all versions type + name to the ComboBox

        	}
        });
                
        versionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionCombo.setPreferredSize(new Dimension(200, 35));
        versionCombo.setMinimumSize(new Dimension(200, 35));
        versionCombo.setMaximumSize(new Dimension(400, 35));
        versionCombo.setBackground(new Color(60, 60, 60));
        versionCombo.setForeground(Color.WHITE);
        versionCombo.setFocusable(false);
        
        btnAction = new JButton("Microsoft");
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAction.setPreferredSize(new Dimension(100, 40));
        btnAction.setMaximumSize(new Dimension(120, 40));
        btnAction.setBackground(new Color(98, 142, 203));
        btnAction.setForeground(Color.WHITE);
        btnAction.setFocusPainted(false);
        btnAction.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAction.setToolTipText("Connexion Microsoft");

        nameLinePanel.add(versionCombo);
        nameLinePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        nameLinePanel.add(btnAction);

        centerPanel.add(nameLinePanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        launchButton = new JButton("Lancer le jeu");
        launchButton.setBackground(new Color(70, 130, 180));
        launchButton.setForeground(Color.WHITE);
        launchButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        launchButton.setFocusPainted(false);
        launchButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        launchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        launchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        SwingUtilities.invokeLater(() -> {
            launchButton.requestFocusInWindow();
        });
        
        centerPanel.add(launchButton);
        JPanel bottomLinePanel = new JPanel();
        bottomLinePanel.setLayout(new BoxLayout(bottomLinePanel, BoxLayout.X_AXIS));
        bottomLinePanel.setBackground(new Color(34, 34, 34));
        
        bottomLinePanel.add(launchButton);
        bottomLinePanel.add(Box.createRigidArea(new Dimension(20, 0))); 
        centerPanel.add(bottomLinePanel);
        
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        btnAction.addActionListener(e -> {
        	btnAction.setEnabled(false);
        	launchButton.setEnabled(false);
        	
        	try {
				AccountData account = Utils.loadAccount();
		        if (account != null) {
		        	int paneResult = JOptionPane.showConfirmDialog(this, "Voulez vous vous reconnecter au compte : " + account.getUsername() + " ?", "Reconnexion ?", JOptionPane.YES_NO_OPTION);
		        	if (paneResult == JOptionPane.YES_OPTION) {
						try {
							loginMicrosoft(authenticator.loginWithRefreshToken(account.getRefreshToken()));
						} catch (MicrosoftAuthenticationException e1) {
							e1.printStackTrace();
		                    JOptionPane.showMessageDialog(this, e1.getStackTrace(), "Erreur", JOptionPane.ERROR_MESSAGE);
						}
		        	} else if (paneResult == JOptionPane.NO_OPTION) {
		        		if (!alreadyTriedToConnect) {
		        			microsoftLoginFrame(authenticator);
		        		} else {
		                	JOptionPane.showMessageDialog(this, "Veuillez r\u00e9executer le launcher pour utiliser la connexion microsoft de nouveau.", "Info", 1);
		                	btnAction.setEnabled(true);
		                	launchButton.setEnabled(true);
		        		}
		        	} else {
		        		btnAction.setEnabled(true);
		        		launchButton.setEnabled(true);
		        		return;
		        	}
	        	} else {
	        		microsoftLoginFrame(authenticator);
        		}
	        } catch (IOException e1) {
				e1.printStackTrace();
			}
        });
        launchButton.addActionListener(e -> {
            if (this.auth == null) {
            	JOptionPane.showMessageDialog(this, "Veuillez vous authentifier avec votre compte microsoft.", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else {
	        	btnAction.setEnabled(false);
	        	launchButton.setEnabled(false);
	        	Utils.versionList.stream().anyMatch((v) -> {
	        		if (versionCombo.getSelectedItem().equals(v.getType() + " " + v.getId())) {
	        			this.startGame(v);
	        			return true;
	        		}
	        		return false;
	        	});
	        }
        });
	}
	
	private void startGame(VersionData versionData){
		BootFrame boot = new BootFrame();
		boot.setVisible(true);
		new Thread(() -> {
			try {
				Utils.launch(versionData, boot, auth);
				boot.dispose();
		        launchButton.setEnabled(true);
			} catch (Exception e) {
				error("Impossible de lancer le jeu : ", e);
				e.printStackTrace();
			}			
		}).start();
	}

	public static void main(String[] args) throws IOException {
		SwingUtilities.invokeLater(() -> {
			try {
				Utils.download(Utils.versionurl, Utils.versionManifest, null);
			} catch (IOException e) {
				error("Impossible de télécharger " + Utils.versionManifest, e);
			}
			(main = new Main()).setVisible(true);
		});
	}
	
	private void microsoftLoginFrame(MicrosoftAuthenticator authenticator) {
		authenticator.loginWithAsyncWebview().thenAccept(result -> {
        	loginMicrosoft(result);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    		launchButton.setEnabled(true);
            return null;
        });
	}
	
	public boolean alreadyTriedToConnect = false;
	public void loginMicrosoft(MicrosoftAuthResult result) {
    	if (result != null) {
    		if (this.auth == null) {
    			String username = result.getProfile().getName();
    			String uuid = result.getProfile().getId();
    			String refreshToken = result.getRefreshToken();
    			String clientId = result.getClientId();
    			String xuid = result.getXuid();
            	this.auth = new Auth(username, uuid, result.getAccessToken(), clientId, xuid);
            	try {
            		Utils.saveAccount(username, refreshToken, uuid, clientId, xuid);
            	} catch (IOException exception) {
            		exception.printStackTrace();
            	}
            	JOptionPane.showMessageDialog(this, "Connect\u00e9 avec le compte " + username, "Connexion avec succès !", 1);
        	} else {
                JOptionPane.showMessageDialog(this, "Une erreur est survenue, veuillez r\u00e9essayer.", "Erreur", JOptionPane.ERROR_MESSAGE);
        	}
    	} else {
        	JOptionPane.showMessageDialog(this, "Veuillez r\u00e9executer le launcher pour utiliser la connexion microsoft de nouveau.", "Info", 1);
        	alreadyTriedToConnect = true;
        	btnAction.setEnabled(true);
    	}
		launchButton.setEnabled(true);
	}
}
