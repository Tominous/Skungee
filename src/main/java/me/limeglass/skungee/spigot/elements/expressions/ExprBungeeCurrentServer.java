package me.limeglass.skungee.spigot.elements.expressions;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.ExpressionType;
import me.limeglass.skungee.objects.packets.SkungeePacket;
import me.limeglass.skungee.objects.packets.SkungeePacketType;
import me.limeglass.skungee.spigot.lang.SkungeeExpression;
import me.limeglass.skungee.spigot.sockets.Sockets;
import me.limeglass.skungee.spigot.utils.annotations.ExpressionProperty;
import me.limeglass.skungee.spigot.utils.annotations.Patterns;

@Name("Bungeecord script server")
@Description("Returns the name of the server that the script calling this syntax is from.")
@Patterns({"[name of] this [script[s]] [bungee[[ ]cord]] server", "[bungee[[ ]cord]] server [name] of this script", "current [bungee[[ ]cord]]server"})
@ExpressionProperty(ExpressionType.SIMPLE)
public class ExprBungeeCurrentServer extends SkungeeExpression<String> {
	
	@Override
	protected String[] get(Event event) {
		String name = (String) Sockets.send(new SkungeePacket(true, SkungeePacketType.CURRENTSERVER, Bukkit.getPort()));
		return (name != null) ? new String[]{name} : null;
	}
}