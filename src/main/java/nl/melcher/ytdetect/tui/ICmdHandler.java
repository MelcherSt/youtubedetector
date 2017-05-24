package nl.melcher.ytdetect.tui;

import java.util.List;

/**
 * Command handler interface
 */
public interface ICmdHandler {

	void handle(List<String> args) throws InvalidArgumentsException;
}
