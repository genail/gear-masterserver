#include <ClanLib/core.h>
#include <ClanLib/application.h>
#include <ClanLib/network.h>

#include <cmath>

// This is the Application class (That is instantiated by the Program Class)
class App
{
	public:
	
		App() : m_disconnected(false) { /* empty */ }
	
		int start(const std::vector<CL_String> &args);
	
		
	private:
	
		bool m_disconnected;
	
	
		void onEventReceived(const CL_NetGameEvent &p_event);
		
		void onDisconnect();
};

// This is the Program class that is called by CL_ClanApplication
class Program
{
public:
	static int main(const std::vector<CL_String> &args)
	{
		// Initialize ClanLib base components
		CL_SetupCore setup_core;

		// Initialize the ClanLib network component
		CL_SetupNetwork setup_network;

		// Start the Application
		App app;
		int retval = app.start(args);
		return retval;
	}
};

// Instantiate CL_ClanApplication, informing it where the Program is located
CL_ClanApplication app(&Program::main);

const char *PORT = "37005";

bool helloAccepted = false;

void App::onDisconnect()
{
	CL_Console::write_line(cl_text("disconnected"));
	m_disconnected = true;
}

void App::onEventReceived(const CL_NetGameEvent &p_event)
{
	CL_Console::write_line(cl_format("event received: %1", p_event.to_string()));
	
	if (!helloAccepted) {
		helloAccepted = true;
		return;
	}
}

// The start of the Application
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
		// Create a console window for text-output if not available
		CL_ConsoleWindow console("Console", 80, 160);
		CL_Console::write_line("Exception caught: " + exception.get_message_and_stack_trace());
		console.display_close_message();

		return -1;
	}
	return 0;
}


