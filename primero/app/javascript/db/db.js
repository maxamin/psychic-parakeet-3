/* eslint-disable class-methods-use-this, no-await-in-loop */
import merge from "deepmerge";
import isEmpty from "lodash/isEmpty";
import { openDB } from "idb";

import { DATABASE_NAME } from "../config/constants";

import subformAwareMerge from "./utils/subform-aware-merge";
import {
  DB_COLLECTIONS_NAMES,
  DB_COLLECTIONS_V1,
  DB_COLLECTIONS_V2,
  DB_COLLECTIONS_V3,
  DB_COLLECTIONS_V4,
  DB_COLLECTIONS_V5,
  TRANSACTION_MODE
} from "./constants";

class DB {
  constructor() {
    if (!DB.instance) {
      const self = this;

      this._db = openDB(DATABASE_NAME, 5, {
        upgrade(db, oldVersion) {
          if (oldVersion < 1) {
            DB_COLLECTIONS_V1.forEach(collection => self.createCollections(collection, db));
          }
          if (oldVersion < 2) {
            DB_COLLECTIONS_V2.forEach(collection => self.createCollections(collection, db));
          }

          if (oldVersion < 3) {
            DB_COLLECTIONS_V3.forEach(collection => self.createCollections(collection, db));
          }

          if (oldVersion < 4) {
            DB_COLLECTIONS_V4.forEach(collection => self.createCollections(collection, db));
          }

          if (oldVersion < 5) {
            DB_COLLECTIONS_V5.forEach(collection => self.createCollections(collection, db));
          }
        }
      });
      DB.instance = this;
    }

    return DB.instance;
  }

  createCollections(collection, db) {
    if (Array.isArray(collection)) {
      const [name, options, index] = collection;

      const store = db.createObjectStore(name, options);

      if (index) store.createIndex(...index);
    } else {
      db.createObjectStore(collection, {
        keyPath: "id",
        autoIncrement: true
      });
    }
  }

  async clearDB() {
    return this.asyncForEach(Object.keys(DB_COLLECTIONS_NAMES), async collection => {
      const store = DB_COLLECTIONS_NAMES[collection];
      const tx = (await this._db).transaction(store, TRANSACTION_MODE.READ_WRITE);

      await tx.objectStore(store).clear();
    });
  }

  async getRecord(store, key) {
    return (await this._db).get(store, key);
  }

  async getAll(store) {
    return (await this._db).getAll(store);
  }

  async getAllFromIndex(store, index, key) {
    return (await this._db).getAllFromIndex(store, index, key);
  }

  async add(store, item) {
    return (await this._db).add(store, item);
  }

  async delete(store, item) {
    return (await this._db).delete(store, item);
  }

  async clear(store) {
    return (await this._db).clear(store);
  }

  async save(isArray = false, args = {}) {
    return isArray ? this.bulkAdd(args) : this.put(args);
  }

  async put({ store, data, key = {}, queryIndex }) {
    const item = data;

    if (queryIndex) {
      item.type = queryIndex.value;
    }

    const tx = (await this._db).transaction(store, TRANSACTION_MODE.READ_WRITE);
    const objectStore = tx.objectStore(store);

    try {
      const prev = await objectStore.get(isEmpty(key) ? item.id : key);

      if (prev) {
        const record = merge(prev, { ...item, ...key }, { arrayMerge: subformAwareMerge });

        await objectStore.put(record);

        await tx.done;

        return record;
      }
      throw new Error("Record is new");
    } catch (e) {
      const record = { ...item, ...key };

      await objectStore.put(record);

      await tx.done;

      return record;
    }
  }

  async asyncForEach(array, callback) {
    for (let index = 0; index < array.length; index += 1) {
      await callback(array[index], index, array);
    }
  }

  async bulkAdd({ store, data, queryIndex }) {
    const isDataArray = Array.isArray(data);
    const tx = (await this._db).transaction(store, TRANSACTION_MODE.READ_WRITE);
    const collection = tx.objectStore(store);
    const records = [];

    this.asyncForEach(isDataArray ? data : Object.keys(data), async record => {
      const r = record;

      if (queryIndex) {
        r.type = queryIndex.value;
      }

      try {
        const prev = await collection.get(isDataArray ? r.id : data[r]?.id);

        if (prev) {
          const item = isDataArray
            ? merge(prev, r, { arrayMerge: subformAwareMerge })
            : merge(prev, data[r], { arrayMerge: subformAwareMerge });

          records.push(item);

          await collection.put(item);
        } else {
          const item = isDataArray ? r : data[r];

          records.push(item);
          await collection.put(item);
        }
      } catch (error) {
        // eslint-disable-next-line no-console
        console.warn(error);
      }
    });

    await tx.done;

    return records;
  }

  async onTransaction(store, mode, callback) {
    const tx = (await this._db).transaction(store, mode);
    const objectStore = tx.objectStore(store);

    let result;

    try {
      result = await callback(tx, objectStore);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.warn(error);
    }

    return result;
  }
}

const instance = new DB();

Object.freeze(instance);

export default instance;
