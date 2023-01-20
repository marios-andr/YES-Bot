package com.congueror.yesbot.window;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.managers.AudioManager;

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
    private static JComboBox<String> textChannels;
    private static JComboBox<String> voiceChannels;

    private static JTextArea message;
    private static JScrollPane msgScrollPane;
    private static JButton send;
    private static JComboBox<String> people;

    private static JButton join;
    private static JButton leave;
    private static JButton mute_all;
    private static JButton unmute_all;

    //button width, height
    private static final int bWidth = 40;
    private static final int bHeight = 40;

    @Nullable
    private static Guild selectedGuild;
    @Nullable
    private static TextChannel selectedTextChannel;
    @Nullable
    private static VoiceChannel selectedVoiceChannel;
    @Nullable
    private static Member selectedMember;

    public static void setup(List<Guild> guilds) {
        f = new JFrame();
        f.setSize(750, 500);

        panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(0x36393F));

        JButton button1 = createMoveButton(true);
        button1.setBounds(10, 10, bWidth, bHeight);
        JButton button2 = createMoveButton(false);
        button2.setBounds(500, 10, bWidth, bHeight);

        for (int i = 0; i < guilds.size(); i++) {
            JButton server = new GuildButton(guilds.get(i));
            server.setBounds(50 + bWidth * i, 10, bWidth, bHeight);
            panel.add(server);
        }

        panel.add(button1);
        panel.add(button2);

        //Text Area
        message = new JTextArea();
        message.setBounds(0, 0, 300, 25);
        message.setEditable(true);
        message.setLineWrap(true);

        msgScrollPane = new JScrollPane(message);
        msgScrollPane.setBounds(10, 90, 300, 60);
        msgScrollPane.setLayout(new ScrollPaneLayout());
        msgScrollPane.setVisible(false);

        panel.add(msgScrollPane);

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
                    selectedTextChannel = channels.stream().filter(s -> s.getName().equals(channel)).findAny().orElse(null);
                    updateTextArea();
                }
            });
            panel.add(textChannels);
            f.repaint();
        }
    }

    private static void updateVoiceChannels() {
        if (voiceChannels != null) {
            panel.remove(voiceChannels);
        }
        if (selectedGuild != null) {
            List<VoiceChannel> channels = selectedGuild.getVoiceChannels();
            voiceChannels = new JComboBox<>();
            for (VoiceChannel a : channels) {
                voiceChannels.addItem(a.getName());
            }
            voiceChannels.setBounds(260, 60, 250, 25);
            voiceChannels.addActionListener(e -> {
                if (e.getSource() instanceof JComboBox<?>) {
                    String channel = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
                    selectedVoiceChannel = channels.stream().filter(s -> s.getName().equals(channel)).findAny().orElse(null);
                    updateVoiceArea();
                }
            });
            panel.add(voiceChannels);
            f.repaint();
        }
    }

    private static void updateTextArea() {
        msgScrollPane.setVisible(false);
        if (send != null) {
            panel.remove(send);
        }
        if (people != null) {
            panel.remove(people);
        }
        if (selectedTextChannel != null) {
            msgScrollPane.setVisible(true);

            BufferedImage image = null;
            try {
                //noinspection ConstantConditions
                image = ImageIO.read(SetupWindow.class.getClassLoader().getResource("send.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert image != null;
            Icon icon = new ImageIcon(resize(image, bWidth, bHeight));
            send = new JButton(icon);
            send.setBounds(310, 90, bWidth, bHeight);
            send.setBorderPainted(false);
            send.setBorder(null);
            send.setMargin(new Insets(0, 0, 0, 0));
            send.setContentAreaFilled(false);
            send.addActionListener(e -> {
                if (selectedTextChannel != null)
                    selectedTextChannel.sendMessage(message.getText()).queue();
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

    private static void updateVoiceArea() {
        if (join != null)
            panel.remove(join);

        if (leave != null)
            panel.remove(leave);

        if (mute_all != null)
            panel.remove(mute_all);

        if (unmute_all != null)
            panel.remove(unmute_all);

        if (selectedVoiceChannel != null) {
            BufferedImage joinImage = null;
            BufferedImage leaveImage = null;
            BufferedImage mute_allImage = null;
            BufferedImage unmute_allImage = null;
            try {
                joinImage = ImageIO.read(SetupWindow.class.getClassLoader().getResource("join.png"));
                leaveImage = ImageIO.read(SetupWindow.class.getClassLoader().getResource("leave.png"));
                mute_allImage = ImageIO.read(SetupWindow.class.getClassLoader().getResource("mute_all.png"));
                unmute_allImage = ImageIO.read(SetupWindow.class.getClassLoader().getResource("unmute_all.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            {
                Icon icon = new ImageIcon(resize(joinImage, bWidth, bHeight));
                join = new JButton(icon);
                join.setBounds(10, 175, bWidth, bHeight);
                join.setBorderPainted(false);
                join.setBorder(null);
                join.setMargin(new Insets(0, 0, 0, 0));
                join.setContentAreaFilled(false);
                join.addActionListener(e -> {
                    if (selectedVoiceChannel != null) {
                        AudioManager audioManager = SetupWindow.selectedGuild.getAudioManager();
                        audioManager.openAudioConnection(selectedVoiceChannel);
                    }
                });
                panel.add(join);
            }

            {
                Icon icon = new ImageIcon(resize(leaveImage, bWidth, bHeight));
                leave = new JButton(icon);
                leave.setBounds(50, 175, bWidth, bHeight);
                leave.setBorderPainted(false);
                leave.setBorder(null);
                leave.setMargin(new Insets(0, 0, 0, 0));
                leave.setContentAreaFilled(false);
                leave.addActionListener(e -> {
                    if (selectedVoiceChannel != null) {
                        AudioManager audioManager = SetupWindow.selectedGuild.getAudioManager();
                        audioManager.closeAudioConnection();
                    }
                });
                panel.add(leave);
            }

            {
                Icon icon = new ImageIcon(resize(mute_allImage, bWidth, bHeight));
                mute_all = new JButton(icon);
                mute_all.setBounds(90, 175, bWidth, bHeight);
                mute_all.setBorderPainted(false);
                mute_all.setBorder(null);
                mute_all.setMargin(new Insets(0, 0, 0, 0));
                mute_all.setContentAreaFilled(false);
                mute_all.addActionListener(e -> {
                    if (selectedVoiceChannel != null) {
                        selectedVoiceChannel.getMembers().forEach(member -> {
                            if (!member.getUser().isBot())
                                member.mute(true).queue();
                        });
                    }
                });
                panel.add(mute_all);
            }

            {
                Icon icon = new ImageIcon(resize(unmute_allImage, bWidth, bHeight));
                unmute_all = new JButton(icon);
                unmute_all.setBounds(130, 175, bWidth, bHeight);
                unmute_all.setBorderPainted(false);
                unmute_all.setBorder(null);
                unmute_all.setMargin(new Insets(0, 0, 0, 0));
                unmute_all.setContentAreaFilled(false);
                unmute_all.addActionListener(e -> {
                    if (selectedVoiceChannel != null) {
                        selectedVoiceChannel.getMembers().forEach(member -> {
                            if (!member.getUser().isBot())
                                member.mute(false).queue();
                        });
                    }
                });
                panel.add(unmute_all);
            }

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
            icon = new ImageIcon(resize(image, bWidth, bHeight));
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

            if (guild.getIconUrl() != null) {
                BufferedImage image = null;
                try {
                    //noinspection ConstantConditions
                    image = ImageIO.read(new URL(guild.getIconUrl()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert image != null;
                Icon icon = new ImageIcon(SetupWindow.resize(image, bWidth, bHeight));
                setIcon(icon);
            } else {
                BufferedImage image = new BufferedImage(bWidth, bHeight, BufferedImage.TYPE_INT_ARGB);
                var a = image.createGraphics();
                a.setColor(new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 1.0f));
                a.fillRect(0, 0, bWidth, bHeight);
                a.dispose();
                Icon icon = new ImageIcon(SetupWindow.resize(image, bWidth, bHeight));
                setIcon(icon);
            }
            setBorderPainted(false);
            setBorder(null);
            setMargin(new Insets(0, 0, 0, 0));
            setContentAreaFilled(false);
            addActionListener(e -> {
                if (e.getSource() instanceof GuildButton b) {
                    SetupWindow.selectedGuild = b.guild;
                    SetupWindow.selectedTextChannel = null;
                    SetupWindow.selectedVoiceChannel = null;
                    updateTextChannels();
                    updateVoiceChannels();
                    updateTextArea();
                    updateVoiceArea();
                }
            });
        }
    }
}
