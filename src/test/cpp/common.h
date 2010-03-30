#pragma once

#include <ClanLib/core.h>
#include <ClanLib/application.h>
#include <ClanLib/network.h>

#define PORT "37005"

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

void App::onDisconnect()
{
	CL_Console::write_line(cl_text("disconnected"));
	m_disconnected = true;
}
