#include <cmath>

#include "common.h"

CL_ClanApplication app(&Program::main);

bool helloAccepted = false;

void App::onEventReceived(const CL_NetGameEvent &p_event)
{
	CL_Console::write_line(cl_format("event received: %1", p_event.to_string()));
	
	if (!helloAccepted) {
		helloAccepted = true;
		return;
	}
}

int App::start(const std::vector<CL_String> &args)
{
	try {
		CL_SlotContainer slots;
	
		CL_NetGameClient client;
		slots.connect(client.sig_event_received(), this, &App::onEventReceived);
		slots.connect(client.sig_disconnected(), this, &App::onDisconnect);
		
		client.connect("localhost", PORT);
		
		CL_NetGameEvent helloEvent("HELLO", 1, 0);
		CL_Console::write_line(cl_format("sending %1", helloEvent.to_string()));
		client.send_event(helloEvent);
		
		while (!helloAccepted) {
			CL_KeepAlive::process();
		}
		
		CL_NetGameEvent registerEvent("REGISTER", 1234, "Test server", "first.map");
		CL_Console::write_line(cl_format("sending %1", registerEvent.to_string()));
		client.send_event(registerEvent);
		
		while (!m_disconnected) {
			CL_KeepAlive::process();
		}
		
		
	} catch(CL_Exception &exception) {
		CL_Console::write_line("Exception caught: " + exception.get_message_and_stack_trace());

		return -1;
	}
	
	return 0;
}


