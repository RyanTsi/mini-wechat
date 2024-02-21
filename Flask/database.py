import pymysql
from config import Config
from time import sleep
MAX_RETRIES = 3
RETRY_DELAY = 0.5


class DataBase:
    db = None

    def __init__(self) -> None:
        try:
            con = Config().database
            self.db = pymysql.connect(
                host=con['host'],
                user=con['user'],
                passwd=con['password'],
                port=con['port'],
                db='weChat',
                connect_timeout=10
            )
        except:
            raise ValueError('connect failure')

    def __del__(self):
        self.db.close()
    # 执行sql命令，如果执行失败抛出错误failure_info,执行成功控制台输出success_info

    def exec(self, sql: str, failure_info: str = "", success_info: str = ""):
        retries = 0
        while retries < MAX_RETRIES:
            try:
                cursor = self.db.cursor()
                cursor.execute(sql)
                self.db.commit()
                cursor.close()  # 关闭游标
                print(success_info)
                return cursor
            except pymysql.Error as e:
                self.db.rollback()
                cursor.close()  # 关闭游标
                cursor = self.db.cursor()  # 重新打开游标
                retries += 1
                sleep(RETRY_DELAY)
                print(e)
                continue
        raise ValueError(failure_info)
