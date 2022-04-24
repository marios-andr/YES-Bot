package com.congueror.yesbot.window;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SetupWindow {

    private static JFrame f;
    private static JPanel panel;
    private static JScrollPane scrollPane;
    private static JComboBox<String> textChannels;
    private static JTextArea message;
    private static JButton send;
    private static JComboBox<String> people;

    private static final int moveWidth = 40;
    private static final int moveHeight = 40;

    @Nullable
    private static Guild selectedGuild;
    @Nullable
    private static TextChannel selectedChannel;
    @Nullable
    private static Member selectedMember;

    public static void setup(List<Guild> guilds) {
        f = new JFrame();
        f.setSize(750, 500);

        message = new JTextArea();
        message.setBounds(0, 0, 300, 25);
        message.setEditable(true);
        message.setLineWrap(true);

        scrollPane = new JScrollPane(message);
        scrollPane.setBounds(10, 90, 300, 60);
        scrollPane.setLayout(new ScrollPaneLayout());
        scrollPane.setVisible(false);

        panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(0x36393F));

        JButton button1 = createMoveButton(true);
        button1.setBounds(10, 10, moveWidth, moveHeight);
        JButton button2 = createMoveButton(false);
        button2.setBounds(500, 10, moveWidth, moveHeight);

        for (int i = 0; i < guilds.size(); i++) {
            JButton server = new GuildButton(guilds.get(i));
            server.setBounds(50 + moveWidth * i, 10, moveWidth, moveHeight);
            panel.add(server);
        }

        panel.add(button1);
        panel.add(button2);

        panel.add(scrollPane);
        f.add(panel);
        f.setVisible(true);
    }

    private static void updateTextChannels() {
        if (textChannels != null) {
            panel.remove(textChannels);
        }
        if (selectedGuild != null) {
            List<TextChannel> channels = selectedGuild.getTextChannels().stream().filter(GuildMessageChannel::canTalk).toList();
            textChannels = new JComboBox<>();
            for (TextChannel a : channels) {
                textChannels.addItem(a.getName());
            }
            textChannels.setBounds(10, 60, 250, 25);
            textChannels.addActionListener(e -> {
                if (e.getSource() instanceof JComboBox<?>) {
                    String channel = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
                    selectedChannel = channels.stream().filter(s -> s.getName().equals(channel)).findAny().orElse(null);
                    updateTextArea();
                }
            });
            panel.add(textChannels);
            f.repaint();
        }
    }

    private static void updateTextArea() {
        scrollPane.setVisible(false);
        if (send != null) {
            panel.remove(send);
        }
        if (people != null) {
            panel.remove(people);
        }
        if (selectedChannel != null) {
            scrollPane.setVisible(true);

            BufferedImage image = null;
            try {
                //noinspection ConstantConditions
                image = ImageIO.read(SetupWindow.class.getClassLoader().getResource("send.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert image != null;
            Icon icon = new ImageIcon(resize(image, moveWidth, moveHeight));
            send = new JButton(icon);
            send.setBounds(310, 90, moveWidth, moveHeight);
            send.setBorderPainted(false);
            send.setBorder(null);
            send.setMargin(new Insets(0, 0, 0, 0));
            send.setContentAreaFilled(false);
            send.addActionListener(e -> {
                if (selectedChannel != null)
                    selectedChannel.sendMessage(message.getText()).queue();
            });
            panel.add(send);

            people = new JComboBox<>();
            for (Member a : selectedGuild.getMembers()) {
                people.addItem(a.getUser().getName());
            }
            people.setBounds(10, 150, 250, 25);
            people.addActionListener(e -> {
                if (e.getSource() instanceof JComboBox<?>) {
                    String member = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
                    selectedMember = selectedGuild.getMembers().stream().filter(s -> s.getUser().getName().equals(member)).findAny().orElse(null);
                    if (selectedMember != null)
                        message.append(selectedMember.getAsMention());
                }
            });
            panel.add(people);


            f.repaint();
        }
    }

    private static JButton createMoveButton(boolean mirror) {
        Icon icon = null;
        try {
            String loc = "move_right.png";
            if (mirror) loc = "move_left.png";
            //noinspection ConstantConditions
            BufferedImage image = ImageIO.read(SetupWindow.class.getClassLoader().getResource(loc));
            icon = new ImageIcon(resize(image, moveWidth, moveHeight));
        } catch (IOException e) {
            e.printStackTrace();
        }
        JButton button = new JButton(icon) {
            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillOval(0, 0, getWidth(), getHeight());
                super.paint(g);
            }
        };
        button.setBackground(new Color(138, 7, 7));
        button.setBorderPainted(false);
        button.setBorder(null);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setContentAreaFilled(false);
        button.addChangeListener(e -> {
            if (e.getSource() instanceof JButton b) {
                if (b.getModel().isRollover()) {
                    b.setBackground(Color.RED);
                } else {
                    b.setBackground(new Color(138, 7, 7));
                }
                b.repaint();
            }
        });

        return button;
    }

    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    public static class GuildButton extends JButton {
        private final Guild guild;

        public GuildButton(Guild guild) {
            super();
            this.guild = guild;

            BufferedImage image = null;
            try {
                //noinspection ConstantConditions
                image = ImageIO.read(new URL(guild.getIconUrl()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert image != null;
            Icon icon = new ImageIcon(SetupWindow.resize(image, moveWidth, moveHeight));
            setIcon(icon);
            setBorderPainted(false);
            setBorder(null);
            setMargin(new Insets(0, 0, 0, 0));
            setContentAreaFilled(false);
            addActionListener(e -> {
                if (e.getSource() instanceof GuildButton b) {
                    SetupWindow.selectedGuild = b.guild;
                    SetupWindow.selectedChannel = null;
                    updateTextChannels();
                    updateTextArea();
                }
            });
        }
    }
}
