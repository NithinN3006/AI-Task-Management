import { useEffect, useState } from 'react';
import { useAuthStore } from '../store/authStore';
import { useTaskStore } from '../store/taskStore';
import type { Task } from '../types';
import TaskModal from '../components/TaskModal';
import TaskHistory from '../components/TaskHistory';
import { Plus, LogOut, Clock, Trash2, Edit2, AlertCircle } from 'lucide-react';

export default function Dashboard() {
  const { user, logout } = useAuthStore();
  const { tasks, loading, error, fetchTasks, updateTaskStatus, deleteTask } = useTaskStore();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const [historyTaskId, setHistoryTaskId] = useState<number | null>(null);

  useEffect(() => {
    fetchTasks();
  }, []);

  const handleLogout = () => {
    logout();
  };

  const handleCreateTask = () => {
    setEditingTask(null);
    setIsModalOpen(true);
  };

  const handleEditTask = (task: Task) => {
    setEditingTask(task);
    setIsModalOpen(true);
  };

  const handleDeleteTask = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this task?')) {
      await deleteTask(id);
    }
  };

  const renderColumn = (status: 'TODO' | 'IN_PROGRESS' | 'DONE', title: string) => {
    const columnTasks = tasks.filter((t) => t.status === status);
    
    return (
      <div className="bg-white/40 backdrop-blur-sm border border-white/50 shadow-sm rounded-2xl p-5 min-h-[500px]">
        <h3 className="font-semibold text-gray-700 mb-4 flex items-center justify-between">
          {title}
          <span className="bg-gray-200 text-gray-600 px-2 py-1 rounded-full text-xs">
            {columnTasks.length}
          </span>
        </h3>
        
        <div className="space-y-4">
          {columnTasks.map((task) => (
            <div key={task.id} className="bg-white/90 backdrop-blur-md p-5 rounded-xl shadow-sm border border-gray-100 hover:shadow-md hover:-translate-y-0.5 transition-all duration-200">
              <div className="flex justify-between items-start mb-2">
                <h4 className="font-medium text-gray-900">{task.title}</h4>
                <div className="flex space-x-1">
                  <button onClick={() => setHistoryTaskId(task.id)} className="p-1 text-gray-400 hover:text-indigo-600 transition-colors" title="View History">
                    <Clock className="w-4 h-4" />
                  </button>
                  <button onClick={() => handleEditTask(task)} className="p-1 text-gray-400 hover:text-blue-600 transition-colors" title="Edit">
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button onClick={() => handleDeleteTask(task.id)} className="p-1 text-gray-400 hover:text-red-600 transition-colors" title="Delete">
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
              
              {task.description && (
                <p className="text-sm text-gray-500 mb-4 line-clamp-2">{task.description}</p>
              )}
              
              <div className="flex items-center justify-between mt-4">
                <span className={`text-xs px-2 py-1 rounded-full font-medium
                  ${task.priority === 'HIGH' ? 'bg-red-100 text-red-700' : 
                    task.priority === 'MEDIUM' ? 'bg-yellow-100 text-yellow-700' : 
                    'bg-green-100 text-green-700'}`}>
                  {task.priority}
                </span>
                
                <select
                  value={task.status}
                  onChange={(e) => updateTaskStatus(task.id, e.target.value)}
                  className="text-xs bg-gray-50 border border-gray-200 rounded px-2 py-1 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                >
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="DONE">Done</option>
                </select>
              </div>
            </div>
          ))}
          {columnTasks.length === 0 && (
            <div className="text-center py-8 text-sm text-gray-400 border-2 border-dashed border-gray-200 rounded-lg">
              No tasks
            </div>
          )}
        </div>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-transparent">
      <header className="bg-white/70 backdrop-blur-md border-b border-gray-200/50 sticky top-0 z-10 shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <h1 className="text-xl font-bold text-gray-900">Task Management</h1>
            <div className="flex items-center gap-4">
              <span className="text-sm text-gray-600">Hello, {user?.name}</span>
              <button
                onClick={handleLogout}
                className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-gray-900 transition-colors"
              >
                <LogOut className="w-4 h-4" />
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="flex justify-between items-center mb-8">
          <h2 className="text-2xl font-bold text-gray-900">Your Tasks</h2>
          <button
            onClick={handleCreateTask}
            className="inline-flex items-center gap-2 bg-indigo-600 text-white px-5 py-2.5 rounded-xl font-medium hover:bg-indigo-700 hover:shadow-lg hover:-translate-y-0.5 transition-all"
          >
            <Plus className="w-5 h-5" />
            New Task
          </button>
        </div>

        {error && (
          <div className="mb-8 bg-red-50 text-red-700 p-4 rounded-lg flex items-center gap-3">
            <AlertCircle className="w-5 h-5" />
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {renderColumn('TODO', 'To Do')}
            {renderColumn('IN_PROGRESS', 'In Progress')}
            {renderColumn('DONE', 'Done')}
          </div>
        )}
      </main>

      {isModalOpen && (
        <TaskModal
          task={editingTask}
          onClose={() => setIsModalOpen(false)}
        />
      )}

      {historyTaskId && (
        <TaskHistory
          taskId={historyTaskId}
          onClose={() => setHistoryTaskId(null)}
        />
      )}
    </div>
  );
}
