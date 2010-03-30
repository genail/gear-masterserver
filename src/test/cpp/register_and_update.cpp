#include <cmath>

#include "common.h"

CL_ClanApplication app(&Program::main);

bool helloAccepted = false;
bool registerAccepted = false;
bool updateAccepted = false;

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
	
	if (!updateAccepted) {
		updateAccepted = true;
		return;
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
		
		
		CL_NetGameEvent registerEvent("REGISTER", 1235, "Update test", "first.map");
		CL_Console::write_line(cl_format("sending %1", registerEvent.to_string()));
		client.send_event(registerEvent);
		
		while (!registerAccepted) {
			CL_KeepAlive::process();
		}
		
		
		reconnect();
		
		CL_NetGameEvent updateEvent("UPDATE", 1235, "Update test - step 2", "second.map");
		CL_Console::write_line(cl_format("sending %1", updateEvent.to_string()));
		client.send_event(updateEvent);
		
		while (!updateAccepted) {
			CL_KeepAlive::process();
		}
		
		
		reconnect();
		m_disconnected = false;
		
		CL_Console::write_line("next update event SHOULD NOT pass");
		
		CL_NetGameEvent updateEvent2("UPDATE", 1236, "Update test - step 3", "third.map");
		CL_Console::write_line(cl_format("sending %1", updateEvent2.to_string()));
		client.send_event(updateEvent2);
		
		
		while (!m_disconnected) {
			CL_KeepAlive::process();
		}
		
	} catch(CL_Exception &exception) {
		CL_Console::write_line("Exception caught: " + exception.get_message_and_stack_trace());

		return -1;
	}
	
	return 0;
}


