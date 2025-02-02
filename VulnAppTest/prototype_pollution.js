async function updateUser(userId, requestBody) {
    const userData = await db.loadUserData(userId);
    merge(userData, requestBody);
  
    log("Saving userData " + userData.toString());
    await db.saveUserData(userId, userData);
    return userData;
  }
  
  async function getRole(userId) {
    const userPermissions = await db.loadUserPermissions(userId);
  
    let role = "user";
    if (userPermissions.role) {
      role = userPermissions.role;
    }
  
    return { role };
  }
  
  /**
   * Sets or updates all attributes of the source object on the target object.
   *
   * For example if `target` is {a: 1, b: 2} and `source` is {a: 3, c: 4},
   * after calling this function `target` becomes {a: 3, b: 2, c: 4}.
   */
  function merge(target, source) {
    for (const attr in source) {
      if (
        typeof target[attr] === "object" &&
        typeof source[attr] === "object"
      ) {
        merge(target[attr], source[attr])
      } else {
        target[attr] = source[attr]
      }
    }
  }
  