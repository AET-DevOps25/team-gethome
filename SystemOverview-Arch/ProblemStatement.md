# 📝 Problem Statement

GetHome addresses two challenges faced by solo pedestrians, particularly in late‑night settings: personal safety and emotional well‑being. 
By combining community‑sourced safety data with an AI “companion,” GetHome guides users along safer walking routes and provides real‑time support during their journey.
The main features include:
- **Safe-Route Planning**: Users can tag locations as unsafe (e.g. parks, alleys,...). GetHome uses this crowd sourced information to calculate an optimal route to avoid these "danger" zones.
- **AI Companion**: When starting a trip, users can talk or chat with a GenAI agent. This companion keeps them company and offers reassurence.
- **Emergencies**: If the AI detects a potentially dangerous situation (e.g. screams, ...) or an emergency button is pressed, GetHome automatically sends the geolocation and potentially a short audio snippet that caused the emergency to pre-configured emergency contacts.

The applications primary audience are individuals walking alone, especially late at night, when friends are sleeping, or through unfamiliar areas. Concerned friends and family who want reassurance that their loved ones can summon help easily and automatically are also an important user group.

The GenAI acts as as a replacement for friends, which maybe are not available. Further it offers needed emotional support and also monitors the users safety, assuring every user, at any time can *GetHome* safely!

### Scenarios
#### Scenario 1: "Normal" Walk Home
1. Anne opens GetHome and enters her destination & GetHome calculates a route avoiding "unsafe" spots flagged by other users. Her emergency contacts get notified that she is now walking home.
2. Once beginning her journey she gets asked if she wants to chat to AI. She clicks yes.
3. Anne talks with the AI about her day. The AI detects raised voices in the background and asks if everything is okay, which Anne answers with yes.
4. Upon reaching home, her emergency contacts get a notification that she arrived home safely. 

#### Scenario 2: Walk with Danger
1. Carlos enters his location and beginns a chat with the companion (same as scenario 1)
2. While walking through the park, the AI notices loud shouting and notices fear in Carlo's voice.
3. The app sends Carlo's location + a audio clip of the situation to his emergency contacts. His roommate calls local authoroties.
4. The park gets marked as unsafe in GetHome

#### Scenario 3: Safety-Mapping
1. Bella encounters a poorly lit underpass and marks it as unsafe
2. GetHome recalculates Bellas route to avoid this passage and the database gets updated.
3. Other users see an updated map and from now on receive routes that avoid Bella's tagged area.
