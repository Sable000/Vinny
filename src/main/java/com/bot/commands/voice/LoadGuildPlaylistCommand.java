package com.bot.commands.voice;

import com.bot.Bot;
import com.bot.commands.VoiceCommand;
import com.bot.db.PlaylistDAO;
import com.bot.models.AudioTrack;
import com.bot.models.Playlist;
import com.bot.utils.CommandPermissions;
import com.bot.utils.Logger;
import com.bot.voice.LoadHandler;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.core.Permission;

import java.util.logging.Level;

public class LoadGuildPlaylistCommand extends VoiceCommand {
    private static final Logger LOGGER = new Logger(LoadGuildPlaylistCommand.class.getName());
    private PlaylistDAO playlistDAO;
    private Bot bot;

    public LoadGuildPlaylistCommand(Bot bot) {
        this.name = "loadgplaylist";
        this.arguments = "<playlist id|playlist name>";
        this.help = "Loads one of the guilds playlists. You must either specify the id or the name of the playlist.";

        this.playlistDAO = PlaylistDAO.getInstance();
        this.bot = bot;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        metricsManager.markCommand(this, commandEvent.getAuthor(), commandEvent.getGuild());
        // Check the permissions to do the command
        if (!CommandPermissions.canExecuteCommand(this, commandEvent))
            return;

        int playlistId = -1;
        String playlistName = null;
        Playlist playlist;
        try {
            // Check if we are given a number (implies playlist id)
            playlistId = Integer.parseInt(commandEvent.getArgs());
        } catch (NumberFormatException e) {
            // if number parsing fails we look for the name;
            playlistName = commandEvent.getArgs();
        }
        String guildId = commandEvent.getGuild().getId();
        playlist = playlistName != null ? playlistDAO.getPlaylistForGuildByName(guildId, playlistName) :
                playlistDAO.getPlaylistForGuildById(guildId, playlistId);

        // If no playlist found then return
        // TODO: Custom exception classes for this stuff.
        if (playlist == null) {
            LOGGER.log(Level.WARNING, "No playlist found for id: " + playlistId + " or name: " + playlistName + "for guild: " + guildId);
            commandEvent.reply(commandEvent.getClient().getWarning() + " Playlist not found! Please check the id/name.");
            return;
        }

        // Check voice perms and what not
        if (commandEvent.getMember().getVoiceState().getChannel() == null) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " You are not in a voice channel! Please join one to use this command.");
            return;
        } else if (!commandEvent.getSelfMember().hasPermission(commandEvent.getMember().getVoiceState().getChannel(), Permission.VOICE_CONNECT)) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " I don't have permission to join your voice channel. :cry:");
            return;
        } else if (!commandEvent.getSelfMember().hasPermission(commandEvent.getMember().getVoiceState().getChannel(), Permission.VOICE_SPEAK)) {
            commandEvent.reply(commandEvent.getClient().getWarning() + " I don't have permission to speak in your voice channel. :cry:");
            return;
        }

        // If not in voice, join
        if (!commandEvent.getGuild().getAudioManager().isConnected()) {
            commandEvent.getGuild().getAudioManager().openAudioConnection(commandEvent.getMember().getVoiceState().getChannel());
        }

        // Queue up the tracks
        for (AudioTrack track : playlist.getTracks()) {
            bot.getManager().loadItemOrdered(commandEvent.getGuild(), track.getUrl(), new LoadHandler(bot, commandEvent));
        }

        commandEvent.reply(commandEvent.getClient().getSuccess() + " Loaded playlist!");

    }
}