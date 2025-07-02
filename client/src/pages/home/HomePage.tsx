import React, { useEffect, useState } from 'react';
import BottomTabBar from '../../components/BottomTabBar';
import { userManagementService } from '../../services/userManagementService';
import { authService } from '../../services/authService';
import { UserProfile, EmergencyContact, Preferences } from '../../types/user';

type TabType = 'overview' | 'contacts' | 'preferences';

const HomePage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(0);
  const [user, setUser] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [tab, setTab] = useState<TabType>('overview');
  const [contactUserCode, setContactUserCode] = useState('');
  const [addContactStatus, setAddContactStatus] = useState<string | null>(null);

  useEffect(() => {
    const fetchProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        const currentUser = await authService.getCurrentUser();
        if (!currentUser) {
          setError('No authenticated user found.');
          setUser(null);
          setLoading(false);
          return;
        }
        const data = await userManagementService.getUserProfile(currentUser.id);
        if (!data) {
          setError('User not found.');
          setUser(null);
        } else {
          setUser(data as UserProfile);
          setError(null);
        }
      } catch (err) {
        setError('Could not load profile.');
        setUser(null);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, []);

  // Handler for adding emergency contact
  const handleAddContact = async () => {
    setAddContactStatus(null);
    try {
      const currentUser = await authService.getCurrentUser();
      if (!currentUser) {
        setAddContactStatus('No authenticated user found.');
        return;
      }
      // You may need to resolve user code to userId in a real app
      await userManagementService.addEmergencyContact(currentUser.id, contactUserCode);
      setAddContactStatus('Contact request sent!');
      setContactUserCode('');
      // Optionally, refresh contacts list here
    } catch (err: any) {
      setAddContactStatus('Failed to add contact: ' + (err?.message || 'Unknown error'));
    }
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      {/* User Info */}
      <div className="flex flex-col items-center justify-center py-8 bg-white shadow">
        <div className="h-16 w-16 rounded-full bg-indigo-600 flex items-center justify-center text-2xl text-white font-bold mb-2">
          {user?.alias?.charAt(0).toUpperCase() ||
            user?.email?.charAt(0).toUpperCase() ||
            '?'}
        </div>
        <div className="text-lg font-semibold">{user?.alias || 'User'}</div>
        <div className="text-gray-500 text-sm">{user?.email}</div>
      </div>

      {/* Tabs */}
      <div className="flex justify-center bg-gray-100 border-b">
        <button
          className={`px-4 py-2 font-medium ${tab === 'overview' ? 'border-b-2 border-indigo-600 text-indigo-600' : 'text-gray-600'}`}
          onClick={() => setTab('overview')}
        >
          Overview
        </button>
        <button
          className={`px-4 py-2 font-medium ${tab === 'contacts' ? 'border-b-2 border-indigo-600 text-indigo-600' : 'text-gray-600'}`}
          onClick={() => setTab('contacts')}
        >
          Emergency Contacts
        </button>
        <button
          className={`px-4 py-2 font-medium ${tab === 'preferences' ? 'border-b-2 border-indigo-600 text-indigo-600' : 'text-gray-600'}`}
          onClick={() => setTab('preferences')}
        >
          Preferences
        </button>
      </div>

      {/* Tab Content */}
      <div className="flex-1 flex flex-col items-center px-4 py-6 w-full">
        {loading ? (
          <div className="text-center text-gray-500">Loading...</div>
        ) : error ? (
          <div className="text-center text-red-500">{error}</div>
        ) : user ? (
          <div className="w-full max-w-md">
            {tab === 'overview' && (
              <div>
                <div className="mb-4">
                  <div className="font-semibold">User Code:</div>
                  <div className="text-gray-700">{user.id || 'N/A'}</div>
                </div>
                <div className="mb-4">
                  <div className="font-semibold">Gender:</div>
                  <div className="text-gray-700">{user.gender || 'Not specified'}</div>
                </div>
                <div className="mb-4">
                  <div className="font-semibold">Age Group:</div>
                  <div className="text-gray-700">{user.ageGroup || 'Not specified'}</div>
                </div>
                <div className="mb-4">
                  <div className="font-semibold">Phone:</div>
                  <div className="text-gray-700">{user.phoneNr || 'Not specified'}</div>
                </div>
                <div className="mb-4">
                  <div className="font-semibold">Preferred Contact:</div>
                  <div className="text-gray-700">{user.preferredContactMethod || 'Not specified'}</div>
                </div>
              </div>
            )}
            {tab === 'contacts' && (
              <div>
                <div className="font-semibold mb-2">Emergency Contacts</div>
                {/* Add Contact Form */}
                <div className="mb-4 flex flex-col sm:flex-row gap-2">
                  <input
                    type="text"
                    className="border rounded px-3 py-2 flex-1"
                    placeholder="Enter contact's user code"
                    value={contactUserCode}
                    onChange={e => setContactUserCode(e.target.value)}
                  />
                  <button
                    className="bg-indigo-600 text-white px-4 py-2 rounded font-semibold"
                    onClick={handleAddContact}
                  >
                    Add
                  </button>
                </div>
                {addContactStatus && (
                  <div className="mb-2 text-sm text-center text-indigo-700">{addContactStatus}</div>
                )}
                {/* List of contacts */}
                {user.emergencyContacts && user.emergencyContacts.length > 0 ? (
                  user.emergencyContacts.map((contact: EmergencyContact, idx: number) => (
                    <div key={idx} className="mb-2 p-2 border rounded">
                      <div className="font-medium">{contact.name}</div>
                      <div className="text-sm text-gray-600">{contact.email}</div>
                      <div className="text-sm text-gray-600">{contact.phone}</div>
                      <div className="text-sm text-gray-600">
                        Preferred: {contact.preferredMethod}
                      </div>
                    </div>
                  ))
                ) : (
                  <div className="text-gray-500">No emergency contacts added.</div>
                )}
              </div>
            )}
            {tab === 'preferences' && (
              <div>
                <div className="font-semibold mb-2">Preferences</div>
                <div className="mb-2">
                  <span className="font-medium">Check-in Interval:</span>{' '}
                  {user.preferences?.checkInInterval ?? 'Not set'} minutes
                </div>
                <div className="mb-2">
                  <span className="font-medium">Share Location:</span>{' '}
                  {user.preferences?.shareLocation ? 'Enabled' : 'Disabled'}
                </div>
                <div className="mb-2">
                  <span className="font-medium">Notify on Delay:</span>{' '}
                  {user.preferences?.notifyOnDelay ? 'Enabled' : 'Disabled'}
                </div>
                <div className="mb-2">
                  <span className="font-medium">Auto Notify Contacts:</span>{' '}
                  {user.preferences?.autoNotifyContacts ? 'Enabled' : 'Disabled'}
                </div>
                <div className="mb-2">
                  <span className="font-medium">SOS Feature:</span>{' '}
                  {user.preferences?.enableSOS ? 'Enabled' : 'Disabled'}
                </div>
              </div>
            )}
          </div>
        ) : null}
      </div>
      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default HomePage;