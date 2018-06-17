package com.bot.commands;

import com.bot.Bot;
import com.bot.db.PlaylistRepository;
import com.bot.voice.QueuedAudioTrack;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SaveMyPlaylistCommand extends Command {
	private PlaylistRepository playlistRepository;
	private Bot bot;

	public SaveMyPlaylistCommand(Bot bot) {
		this.bot = bot;
		playlistRepository = PlaylistRepository.getInstance();
		this.name = "savemyplaylist";
		this.arguments = "Name";
		this.help = "Saves the current audio playlist as a playlist accessible for any server you are on.";
	}

	@Override
	protected void execute(CommandEvent commandEvent) {
		// TODO: Take current playlist/range and save it to DB

		String args = commandEvent.getArgs();
		if (args.equals("")) {
			commandEvent.reply("You need to specify a name for the playlist.");
			return;
		}

		LinkedList<QueuedAudioTrack> tracks = new LinkedList<>(bot.getHandler(commandEvent.getGuild()).getTracks());
		QueuedAudioTrack nowPlaying = bot.getHandler(commandEvent.getGuild()).getNowPlaying();

		List<QueuedAudioTrack> trackList = new LinkedList<>();
		trackList.add(nowPlaying);
		trackList.addAll(tracks);

		if (playlistRepository.createPlaylistForUser(commandEvent.getAuthor().getId(), args, trackList)) {
			commandEvent.reply("Playlist successfully created.");
		} else {
			commandEvent.reply("Something went wrong! Failed to create playlist.");
		}
	}
}