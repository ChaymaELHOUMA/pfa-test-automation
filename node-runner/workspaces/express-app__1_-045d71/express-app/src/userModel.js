// Simule une base de données en mémoire pour les utilisateurs
let users = [
  { id: 1, name: "Alice", email: "alice@example.com" },
  { id: 2, name: "Bob", email: "bob@example.com" }
];
let nextId = 3;

function getAll() {
  return users;
}

function getById(id) {
  return users.find((u) => u.id === id);
}

function create({ name, email }) {
  const newUser = { id: nextId++, name, email };
  users.push(newUser);
  return newUser;
}

function update(id, { name, email }) {
  const user = getById(id);
  if (!user) return null;
  if (name !== undefined) user.name = name;
  if (email !== undefined) user.email = email;
  return user;
}

function remove(id) {
  const index = users.findIndex((u) => u.id === id);
  if (index === -1) return false;
  users.splice(index, 1);
  return true;
}

// Utile pour réinitialiser l'état entre les tests
function reset() {
  users = [
    { id: 1, name: "Alice", email: "alice@example.com" },
    { id: 2, name: "Bob", email: "bob@example.com" }
  ];
  nextId = 3;
}

module.exports = { getAll, getById, create, update, remove, reset };
