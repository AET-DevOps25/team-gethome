import React, { useState } from 'react';
import BottomTabBar from '../../components/BottomTabBar';

const categories = [
  { value: '', label: 'Select category' },
  { value: 'area-was-not-lit', label: 'Area was not lit' },
  { value: 'high-insecurity', label: 'High insecurity' },
  { value: 'bad-signal', label: 'Bad signal' },
  { value: 'other', label: 'Other' },
];

const ReportsPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState(3); // 3 for Flags
  const [form, setForm] = useState({
    category: '',
    description: '',
    location: '',
  });
  const [submitted, setSubmitted] = useState(false);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitted(true);
    // Here you would send the form data to your backend
    setTimeout(() => setSubmitted(false), 2000);
    setForm({ category: '', description: '', location: '' });
  };

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <div className="flex-1 flex flex-col items-center justify-center px-4 py-8">
        <form
          onSubmit={handleSubmit}
          className="w-full max-w-md bg-white rounded-lg shadow p-6"
        >
          <h2 className="text-xl font-bold mb-4 text-center">Report an Incident</h2>
          <label className="block mb-2 font-medium">Category</label>
          <select
            name="category"
            value={form.category}
            onChange={handleChange}
            required
            className="w-full mb-4 px-3 py-2 border rounded"
          >
            {categories.map((cat) => (
              <option key={cat.value} value={cat.value}>{cat.label}</option>
            ))}
          </select>

          <label className="block mb-2 font-medium">Where did it happen?</label>
          <input
            name="location"
            value={form.location}
            onChange={handleChange}
            required
            placeholder="e.g. Main St & 3rd Ave"
            className="w-full mb-4 px-3 py-2 border rounded"
          />

          <label className="block mb-2 font-medium">Description</label>
          <textarea
            name="description"
            value={form.description}
            onChange={handleChange}
            required
            placeholder="Describe what happened..."
            className="w-full mb-4 px-3 py-2 border rounded resize-none"
            rows={3}
          />

          <button
            type="submit"
            className="w-full bg-indigo-600 text-white py-2 rounded font-semibold"
          >
            Submit Report
          </button>
          {submitted && (
            <div className="text-green-600 text-center mt-2">Report submitted!</div>
          )}
        </form>
      </div>
      <BottomTabBar activeTab={activeTab} setActiveTab={setActiveTab} />
    </div>
  );
};

export default ReportsPage;