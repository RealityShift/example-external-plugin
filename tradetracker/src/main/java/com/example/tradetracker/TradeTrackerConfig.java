/*
 * Copyright (c) 2018, Andrew EP | ElPinche256 <https://github.com/ElPinche256>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.tradetracker;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("tradetracker")
public interface TradeTrackerConfig extends Config {

	@ConfigItem(
			keyName = "onlyShowWhitelist",
			name = "Only Show Whitelist",
			description = "Hides trades from all not whitelisted",
			position = 1
	)
	default boolean onlyShowWhitelist() { return false; }

	@ConfigItem(
			keyName = "whitelistedPlayers",
			name = "Whitelisted Players",
			description = "List of whitelisted players, one name per line. ALWAYS overrides other checks.",
			position = 2
	)
	default String whitelistedPlayers() { return ""; }

	@ConfigItem(
			keyName = "amountTraded",
			name = "Amount Traded",
			description = "Amount traded in long form (eg. 3,000,000)",
			position = 3
	)
	default String amountTraded()
	{
		return "";
	}

	@ConfigItem(
			keyName = "showAdvertisers",
			name = "Show Advertiser TRADEREQS",
			description = "Shows trade requests from advertisers",
			position = 4
	)
	default boolean onlyShowAdvertisers() { return false; }

	@ConfigItem(
			keyName = "hidePaidAdvertisers",
			name = "Hide Paid Advertiser TRADEREQS",
			description = "Hide trade requests from advertisers who have been paid",
			position = 5
	)
	default boolean hidePaidAdvertisers() { return true; }

	@ConfigItem(
			keyName = "adWords",
			name = "Words that must be in the message",
			description = "One word per line. Each word is required in a single message.",
			position = 5
	)
	default String adWords() { return ""; }


}
