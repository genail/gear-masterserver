#include <cmath>

#include "common.h"

CL_ClanApplication app(&Program::main);

bool helloAccepted = false;
bool registerAccepted = false;

void App::onEventReceived(const CL_NetGameEvent &p_event)
{
	CL_Console::write_line(cl_format("event received: %1", p_event.to_string()));
	
	if (!helloAccepted) {
		helloAccepted = true;
		return;
	}
	
	if (!registerAccepted) {
		registerAccepted = true;
		return;
	}
	
	if (p_event.get_name() == cl_text("SERVERLIST")) {
	
		static const int HEADER_SIZE = 2;
	
		const int entrySize = p_event.get_argument(0);
		const int entryCount = p_event.get_argument(1);
		
		for (int entryIdx = 0; entryIdx < entryCount; ++entryIdx) {
			int i = HEADER_SIZE + (entrySize * entryIdx);
			
			CL_String serverAddr = p_event.get_argument(i++);
			int serverPort = p_event.get_argument(i++);
			CL_String serverName = p_event.get_argument(i++);
			CL_String mapName = p_event.get_argument(i++);
			
			CL_Console::write_line(cl_format("%1:%2 - %3 (%4)", serverAddr, serverPort, serverName, mapName));
		}
	}
}

CL_NetGameClient client;

void reconnect() {
	CL_Console::write_line("connecting...");
	
	helloAccepted = false;
	client.connect("localhost", PORT);
	
	CL_NetGameEvent helloEvent("HELLO", VERSION_MAJOR, VERSION_MINOR);
	CL_Console::write_line(cl_format("sending %1", helloEvent.to_string()));
	client.send_event(helloEvent);
	
	while (!helloAccepted) {
		CL_KeepAlive::process();
	}
}

int App::start(const std::vector<CL_String> &args)
{
	try {
		CL_SlotContainer slots;
	
		slots.connect(client.sig_event_received(), this, &App::onEventReceived);
		slots.connect(client.sig_disconnected(), this, &App::onDisconnect);
		
		reconnect();
		
		
		for (int i = 1; i <= 10; ++i) {
			CL_NetGameEvent registerEvent("REGISTER", 2000 + i, cl_format("List test server %1", i), cl_format("%1.map", i));
			CL_Console::write_line(cl_format("sending %1", registerEvent.to_string()));
			client.send_event(registerEvent);
			
			while (!registerAccepted) {
				CL_KeepAlive::process();
			}
			
			
			reconnect();
		}
		
		m_disconnected = false;
		
		CL_NetGameEvent listEvent("LISTREQUEST");
		CL_Console::write_line(cl_format("sending %1", listEvent.to_string()));
		client.send_event(listEvent);
		
		
		while (!m_disconnected) {
			CL_KeepAlive::process();
		}
		
	} catch(CL_Exception &exception) {
		CL_Console::write_line("Exception caught: " + exception.get_message_and_stack_trace());

		return -1;
	}
	
	return 0;
}


