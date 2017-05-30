package nl.melcher.ytdetect.tui.handler;

import nl.melcher.ytdetect.tui.InvalidArgumentsException;

import java.util.List;

/**
 * Command handler interface
 */
public interface ICmdHandler {

	void handle(List<String> args) throws InvalidArgumentsException;
}
