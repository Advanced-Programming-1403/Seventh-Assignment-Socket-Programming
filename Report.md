
# Report.md

## 🧠 Theoretical Questions (Answered as a Java Beginner)

---

### ✅ Method 1: Plain String Format

```java
stringOut.println("LOGIN|" + loginRequest.username + "|" + loginRequest.password);
```

#### 🔍 What are the pros and cons?

**Pros:**
- It's super easy! Just combine text and send.
- I can read and understand it without any tools.
- Works right away with `PrintWriter` and `BufferedReader`.

**Cons:**
- If I accidentally use the `|` symbol in my username or password, everything breaks. 
- I have to remember the exact order (is password second or third?).
- Not good if I want to send more than just a username and password (like a profile pic or settings).

#### 🧠 How do I parse it?

I split the message like this:
```java
String[] parts = message.split("\|");
String command = parts[0];
String username = parts[1];
String password = parts[2];
```

But I have to be careful that the array has 3 parts or it throws an error.

#### 🚫 Is it good for complex data?

Nope. It's okay for login, but if I had to send user settings, roles, and notifications... it would get messy real quick.

---

### ✅ Method 2: Serialized Java Object

```java
ObjectOutputStream objectOut = new ObjectOutputStream(socket.getOutputStream());
objectOut.writeObject(loginRequest);
```

#### 🔍 Why send a full object?

**Pros:**
- I don't need to split or parse anything. I can just send the whole login form.
- I get all the variables bundled together — like a nice little package 📦.

**Cons:**
- It only works with Java 😢 — Python or JavaScript wouldn't know what to do with this object.
- Sometimes it throws errors if the class doesn't implement `Serializable`.
- I found it a bit tricky when reading objects on the server side — especially if versions don't match.

#### 🧪 Verdict:

Feels like magic when it works — but I wouldn't use it if I had friends coding in other languages.

---

### ✅ Method 3: JSON

```java
Gson gson = new Gson();
String json = gson.toJson(loginRequest);
jsonOut.println(json);
```

####  Why is JSON preferred?

**Pros:**
- It works everywhere! I tested the same JSON on a JavaScript server.
- It’s human-readable and also machine-readable.
- I can nest objects or arrays if I need to.

**Cons:**
- I need to add Gson or another library to handle JSON.
- If the field names change, the parsing can break.

#### 💡 Would it work across languages?



---

### 📦 Summary (From My Beginner Brain):

| Method       | Easy? | Cross-platform? | Good for Complex Data? |
|--------------|-------|------------------|--------------------------|
| Plain String | ✅    | ❌               | ❌                       |
| Java Object  | ❌    | ❌               | ✅ (in Java only)        |
| JSON         | ✅    | ✅               | ✅                       |

