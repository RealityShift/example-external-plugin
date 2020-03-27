package com.example.tradetracker;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MessageNode;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.util.Text;
import org.pf4j.Extension;
import net.runelite.client.plugins.PluginType;

import javax.inject.Inject;

import java.util.ArrayList;

import static net.runelite.api.ChatMessageType.PUBLICCHAT;
import static net.runelite.api.ChatMessageType.TRADEREQ;

@Extension
@Slf4j
@PluginDescriptor(
        name = "Trade Tracker",
        description = "Hides trades from players already traded",
        tags = {"farming", "minigame", "overlay", "skilling", "timers"},
        type = PluginType.UTILITY
)
public class TradeTrackerPlugin extends Plugin {

    private final CharMatcher jagexPrintableCharMatcher = Text.JAGEX_PRINTABLE_CHAR_MATCHER;
    private static final Splitter NEWLINE_SPLITTER = Splitter
            .on("\n")
            .omitEmptyStrings()
            .trimResults();

    @Inject
    private Client client;

    @Inject
    private TradeTrackerConfig config;

    @Provides
    TradeTrackerConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(TradeTrackerConfig.class);
    }

    private ArrayList<String> usersTradedAlready = new ArrayList<String>();
    private ArrayList<String> whiteListedPlayers = new ArrayList<>();
    private ArrayList<String> advertisers = new ArrayList<>();
    private ArrayList<String> adWords = new ArrayList<>();
    private String amountTraded;
    private boolean onlyShowWhitelist;
    private boolean showAdRequests;
    private boolean hidePaidAdvertisers;

    @Override
    protected void startUp() throws Exception {
        updateWhiteListedNames();
        updateAdWords();
        amountTraded = config.amountTraded();
        onlyShowWhitelist = config.onlyShowWhitelist();
        showAdRequests = config.onlyShowAdvertisers();
        hidePaidAdvertisers = config.hidePaidAdvertisers();

        client.refreshChat();
    }

    @Override
    protected void shutDown() throws Exception {
        usersTradedAlready.clear();


        client.refreshChat();
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!"tradetracker".equals(event.getGroup())) {
            return;
        }

        amountTraded = config.amountTraded();
        onlyShowWhitelist = config.onlyShowWhitelist();
        showAdRequests = config.onlyShowAdvertisers();
        hidePaidAdvertisers = config.hidePaidAdvertisers();
        updateWhiteListedNames();
        updateAdWords();

        //Refresh chat after config change to reflect current rules
        client.refreshChat();
    }

    @Subscribe
    public void onScriptCallbackEvent(ScriptCallbackEvent event) {
        if (!"chatFilterCheck".equals(event.getEventName())) {
            return;
        } else {
            int[] intStack = client.getIntStack();
            int intStackSize = client.getIntStackSize();
            int messageType = intStack[intStackSize - 2];
            int messageId = intStack[intStackSize - 1];

            ChatMessageType chatMessageType = ChatMessageType.of(messageType);
            MessageNode messageNode = client.getMessages().get(messageId);
            String playerName = getPlayersName(messageNode.getName());

            if (chatMessageType == TRADEREQ) {

                // if user is whitelisted, always show them
                if (whiteListedPlayers.contains(playerName)) {
                    return;
                } else {

                    // If only whitelist, don't show any
                    if (onlyShowWhitelist) {
                        intStack[intStackSize - 3] = 0;
                    } else {
                        // if show ads is on, show trades if user has advertised
                        if (showAdRequests) {
                            if (advertisers.contains(playerName)) {
                                String oldmessage = messageNode.getValue();

                                if (usersTradedAlready.contains(playerName)) {

                                    String[] stringStack = client.getStringStack();
                                    int stringStackSize = client.getStringStackSize();

                                    //String message = stringStack[stringStackSize - 1];
                                    stringStack[stringStackSize - 1] = oldmessage + " - PAID!";
                                } else {
                                    if (!oldmessage.contains("seen advertising")) {
                                        String[] stringStack = client.getStringStack();
                                        int stringStackSize = client.getStringStackSize();

                                        //String message = stringStack[stringStackSize - 1];
                                        stringStack[stringStackSize - 1] = oldmessage + " - seen advertising.";
                                    }
                                }
                            } else {
                                intStack[intStackSize - 3] = 0;
                            }
                        }

                        // If hide paid ads is on, remove trades from players who have been paid
                        if (hidePaidAdvertisers) {
                            if (usersTradedAlready.contains(playerName)) {
                                intStack[intStackSize - 3] = 0;
                            }
                        }
                    }
                }
            } else if (chatMessageType == PUBLICCHAT) {
                boolean allWordsFound = true;

                String playersMessage = messageNode.getValue().toLowerCase();

                // for each word in adwords, make sure the message contains it
                for (String word : adWords) {
                    if (!playersMessage.contains(word.toLowerCase())) {
                        allWordsFound = false;
                    }
                }

                // if all words found and user isn't in advertisers group, add them
                if (allWordsFound) {
                    if (!advertisers.contains(playerName)) {
                        advertisers.add(playerName);
                    }
                }
            }
        }
    }

//    @Subscribe
//    public void onChatMessage(ChatMessage chatMessage) {
//        MessageNode messageNode = chatMessage.getMessageNode();
//        boolean update = false;
//
//        switch (chatMessage.getType())
//        {
//            case TRADEREQ:
//                System.out.println(chatMessage);
//
//                String playerName = messageNode.getName();
//                String fakeplayer = "Alt nation";
//
//                playerName = playerName.replace(' ', ' ');
//
//                if (usersTradedAlready.contains(playerName)) {
//                    messageNode.setValue(messageNode.getValue() + " -- Already Paid ads.");
//                }
//                break;
//        }
//    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded event) {
        System.out.println("Widget!");
        System.out.println(event);

        if (event.getGroupId() == 334) {
            Widget widget = client.getWidget(334, 1);
            //Widget widget = client.getWidget(WidgetInfo.PLAYER_TRADE_FINAL_SCREEN);
            // get player name
            // get my contents
            String playerName = widget.getStaticChildren()[27].getText();
            String tradedItem = widget.getStaticChildren()[25].getDynamicChildren()[0].getText();

            playerName = playerName.split("<br>")[1];
            playerName = getPlayersName(playerName);

            if (tradedItem.contains("Coins") && tradedItem.contains(amountTraded)) {
                System.out.println("traded " + amountTraded + " to " + playerName);
                usersTradedAlready.add(playerName);
            }
        }
    }

    void updateWhiteListedNames() {
        whiteListedPlayers.clear();

        NEWLINE_SPLITTER.splitToList(config.whitelistedPlayers()).stream().forEach((player) -> whiteListedPlayers.add(player.toLowerCase()));
    }

    void updateAdWords() {
        adWords.clear();

        NEWLINE_SPLITTER.splitToList(config.adWords()).stream().forEach((word) -> adWords.add(word.toLowerCase()));
    }

    String getPlayersName(String message) {
        return jagexPrintableCharMatcher.retainFrom(message).replace('\u00A0', ' ').toLowerCase();
    }
}