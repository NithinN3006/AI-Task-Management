import { useState, useEffect } from 'react';
import { api } from '../lib/api';
import type { TaskLedger } from '../types';
import { ShieldCheck, ShieldAlert, Clock } from 'lucide-react';

interface TaskHistoryProps {
  taskId: number;
  onClose: () => void;
}

export default function TaskHistory({ taskId, onClose }: TaskHistoryProps) {
  const [history, setHistory] = useState<TaskLedger[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [verifyStatus, setVerifyStatus] = useState<{valid: boolean, message: string} | null>(null);

  useEffect(() => {
    fetchHistory();
    verifyLedger();
  }, [taskId]);

  const fetchHistory = async () => {
    try {
      const response = await api.get(`/tasks/${taskId}/history`);
      setHistory(response.data);
    } catch (err: any) {
      setError('Failed to load history');
    } finally {
      setLoading(false);
    }
  };

  const verifyLedger = async () => {
    try {
      const response = await api.get(`/tasks/${taskId}/verify`);
      setVerifyStatus(response.data);
    } catch (err: any) {
      setVerifyStatus({ valid: false, message: 'Verification failed' });
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-3xl max-h-[90vh] flex flex-col overflow-hidden">
        <div className="flex justify-between items-center p-6 border-b border-gray-100">
          <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
            <Clock className="w-5 h-5 text-indigo-500" />
            Immutable Task History (Ledger)
          </h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition">
            <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="p-6 overflow-y-auto flex-1 bg-gray-50">
          {verifyStatus && (
            <div className={`mb-6 p-4 rounded-lg flex items-center gap-3 ${verifyStatus.valid ? 'bg-green-50 text-green-800 border border-green-200' : 'bg-red-50 text-red-800 border border-red-200'}`}>
              {verifyStatus.valid ? <ShieldCheck className="w-6 h-6 text-green-600" /> : <ShieldAlert className="w-6 h-6 text-red-600" />}
              <div>
                <p className="font-semibold">{verifyStatus.valid ? 'Ledger Verified' : 'Ledger Tampered'}</p>
                <p className="text-sm opacity-90">{verifyStatus.message}</p>
              </div>
            </div>
          )}

          {loading ? (
            <div className="text-center py-8 text-gray-500">Loading immutable history...</div>
          ) : error ? (
            <div className="text-center py-8 text-red-500">{error}</div>
          ) : history.length === 0 ? (
            <div className="text-center py-8 text-gray-500">No history found.</div>
          ) : (
            <div className="space-y-6">
              {history.map((entry) => (
                <div key={entry.id} className="relative pl-8 before:absolute before:left-[11px] before:top-8 before:bottom-[-24px] before:w-0.5 before:bg-indigo-100 last:before:hidden">
                  <div className="absolute left-0 top-1.5 w-6 h-6 rounded-full bg-indigo-100 border-4 border-white flex items-center justify-center shadow-sm">
                    <div className={`w-2 h-2 rounded-full ${entry.valid ? 'bg-indigo-500' : 'bg-red-500'}`} />
                  </div>
                  <div className="bg-white p-5 rounded-lg border border-gray-100 shadow-sm">
                    <div className="flex justify-between items-start mb-3">
                      <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-indigo-100 text-indigo-800">
                        {entry.action}
                      </span>
                      <span className="text-xs text-gray-500">
                        {new Date(entry.timestamp).toLocaleString()}
                      </span>
                    </div>
                    
                    <div className="text-xs font-mono bg-gray-50 p-3 rounded text-gray-600 overflow-x-auto mb-3">
                      {entry.payloadSnapshot}
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs font-mono text-gray-500 bg-gray-50 p-3 rounded">
                      <div>
                        <span className="font-semibold block text-gray-700 mb-1">Previous Hash:</span>
                        <span className="truncate block" title={entry.prevHash}>{entry.prevHash}</span>
                      </div>
                      <div>
                        <span className="font-semibold block text-gray-700 mb-1">Current Hash:</span>
                        <span className="truncate block" title={entry.hash}>{entry.hash}</span>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
