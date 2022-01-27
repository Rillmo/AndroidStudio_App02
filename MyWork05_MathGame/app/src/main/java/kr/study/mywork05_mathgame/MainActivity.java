package kr.study.mywork05_mathgame;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    Bitmap screen;  // 배경 Bitmap
    int Width;  // 디스플레이 너비
    int Height; // 디스플레이 높이

    Bitmap balloonimg; // 풍선 Bitmap

    Bitmap basket;  // 바구니 Bitmap
    int basket_width;   // 바구니 너비
    int basket_height;  // 바구니 높이
    int basket_x, basket_y; // 바구니 위치

    Bitmap leftimg;     // 왼쪽 버튼 Bitmap
    int left_x, left_y; // 왼쪽 버튼 위치
    Bitmap rightimg;    // 오른쪽 버튼 Bitmap
    int right_x, right_y;   // 오른쪽 버튼 위치
    int button_width;   // 버튼 너비

    int score;  // 점수
    int num1, num2; // 덧셈에 사용될 숫자
    int answer; // 정답
    int [] wrongNum = new int[5];

    AnswerBalloon answerBalloon;// 정답 풍선 객체 생성

    ArrayList<Balloon> balloons;    // 오답 풍선 ArrayList 생성


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        Width = metrics.widthPixels;    // 사용중인 기기의 해상도(가로) 크기 저장
        Height = metrics.heightPixels;  // 사용중인 기기의 해상도(세로) 크기 저장

        // 배경 Bitmap 저장
        screen = BitmapFactory.decodeResource(getResources(), R.drawable.screen);
        screen = Bitmap.createScaledBitmap(screen, Width, Height, true);

        // 버튼 너비 저장
        button_width = Width / 6;
        // 버튼 위치 저장
        left_x = Width*2/10-button_width;
        left_y = Height*8/10;
        right_x = Width*8/10;
        right_y = Height*8/10;
        // 버튼 Bitmap 저장
        leftimg = BitmapFactory.decodeResource(getResources(), R.drawable.left);
        leftimg = Bitmap.createScaledBitmap(leftimg, button_width, button_width, true);
        rightimg = BitmapFactory.decodeResource(getResources(), R.drawable.right);
        rightimg = Bitmap.createScaledBitmap(rightimg, button_width, button_width, true);

        // 바구니 너비,높이 저장
        basket_width = Width/4;
        basket_height = Height/14;
        // 바구니 위치 저장
        basket_x = Width*1/2;
        basket_y = Height*6/9;
        // 바구니 Bitmap 저장
        basket = BitmapFactory.decodeResource(getResources(), R.drawable.basket);
        basket = Bitmap.createScaledBitmap(basket, basket_width, basket_height, true);

        // 풍선 Bitmap 저장
        balloonimg = BitmapFactory.decodeResource(getResources(), R.drawable.balloon);
        balloonimg = Bitmap.createScaledBitmap(balloonimg, button_width, button_width, true);

        Random random = new Random();
        int xx = random.nextInt(Width-button_width);
        // 정답 풍선 객체 주소 할당
        answerBalloon = new AnswerBalloon(xx, 0, 5);

        // 오답 풍선 ArrayList 주소 할당
        balloons = new ArrayList<Balloon>();
    }


    class MyView extends View {
        MyView(Context context) {
            super(context);
            setBackgroundColor(Color.GREEN);
            myHandler.sendEmptyMessageDelayed(0, 1000);
            makeQuestion();
        }


        @Override
        public void onDraw(Canvas canvas){
            Paint p1 = new Paint();
            p1.setColor(Color.WHITE);
            p1.setTextSize(Width/20);

            canvas.drawBitmap(screen, 0, 0, p1);

            canvas.drawText("점수 : " + Integer.toString(score), 0,Height*1/12,p1);
            canvas.drawText("문제 : " + Integer.toString(num1) + "+" + Integer.toString(num2), 0,Height*2/12,p1);
            canvas.drawBitmap(leftimg, left_x, left_y, p1);
            canvas.drawBitmap(rightimg, right_x, right_y, p1);
            canvas.drawBitmap(basket, basket_x, basket_y, p1);

            // 정답 풍선 그리기
            canvas.drawBitmap(balloonimg, answerBalloon.x, answerBalloon.y, p1);
            canvas.drawText(Integer.toString(answer), answerBalloon.x+button_width*1/3,
                    answerBalloon.y+button_width*1/2, p1);

            // 오답 풍선 그리기
            for (Balloon tmp : balloons){
                canvas.drawBitmap(balloonimg, tmp.x, tmp.y, p1);
            }

            // 오답 풍선 숫자 넣기
            for (int i=balloons.size()-1; i>=0; i--){
                canvas.drawText(Integer.toString(wrongNum[i]), balloons.get(i).x+button_width*1/3,
                        balloons.get(i).y+button_width*1/2, p1);
            }

            // 오답 풍선 만들기
            if(balloons.size() < 5){
                Random random = new Random();
                int x, y;
                x = random.nextInt(Width-button_width);
                y = random.nextInt(Height/4);
                balloons.add(new Balloon(x, y, 5));
            }

            // 풍선 이동
            moveBalloon();
            // 충돌 확인
            collisionCheck();
        }

        public void moveBalloon(){  // 풍선 이동 메소드
            Random random = new Random();

            // 정답 풍선 이동
            if(answerBalloon.y > Height){
                answerBalloon.y = -100;
                answerBalloon.x = random.nextInt(Width-button_width);
            }
            answerBalloon.move();

            // 오답 풍선이 바닥 아래로 사라지면 위에서 다시 나옴
            for (int i=balloons.size()-1; i>=0; i--){
                if(balloons.get(i).y > Height)  {
                    balloons.get(i).y = -100;
                    balloons.get(i).x = random.nextInt(Width-button_width);
                }
            }

            // 오답 풍선 이동
            for (int i=balloons.size()-1; i>=0; i--){
                balloons.get(i).move();
            }
        }

        public void makeQuestion(){ // 문제 만들기 메소드
            Random random = new Random();

            // 정답 풍선에 들어갈 숫자
            num1 = random.nextInt(99) + 1;
            num2 = random.nextInt(99) + 1;
            answer = num1 + num2;

            int result;
            // 오답 풍선에 들어갈 숫자
            for (int i=0; i<5; i++){
                result = random.nextInt(197) + 1;
                if(result == answer)    result = random.nextInt(197) + 1;
                wrongNum[i] = result;
            }
        }

        public void collisionCheck() {  // 풍선과 바구니 충돌 처리

            // 정답 바구니 처리
            if((basket_x<answerBalloon.x+button_width/2 && answerBalloon.x+button_width/2<basket_x+basket_width)
                    && (answerBalloon.y+button_width > basket_y && answerBalloon.y+button_width<basket_y+basket_height/2)){
                makeQuestion(); // 새 문제 생성
                int xx;
                Random random = new Random();
                xx = random.nextInt(Width-button_width);
                answerBalloon.x = xx;
                answerBalloon.y = -200;
                score += 30;
            }

            // 오답 바구니 처리
            for (int i=balloons.size()-1; i>=0; i--){
                if((basket_x<balloons.get(i).x+button_width/2 && balloons.get(i).x+button_width/2<basket_x+basket_width)
                    && (balloons.get(i).y+button_width>basket_y && balloons.get(i).y+button_width<basket_y+basket_height/2)){
                    balloons.remove(i);
                    score -= 10;
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = 0, y = 0;
            // 터치했을 경우 처리
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE){
                x = (int)event.getX();
                y = (int)event.getY();
                // 터치한 곳이 왼쪽 버튼일 경우
                if ((x>left_x && x<left_x+button_width) && (y>left_y && y<left_y+button_width)){
                    if(basket_x>0)
                        basket_x -= 5;
                }
                // 터치한 곳이 오른쪽 버튼일 경우
                if ((x>right_x && x<right_x+button_width) && (y>right_y && y<right_y+button_width)) {
                    if (basket_x+basket_width<Width)
                        basket_x += 5;
                }
            }
            return true;
        }

        // Handler 클래스 이용해서 invalidate() 반복호출

        MyHandler myHandler = new MyHandler();

        class MyHandler extends Handler {
            @Override
            public void handleMessage(@NonNull Message msg) {
                invalidate();
                myHandler.sendEmptyMessageDelayed(0, 30);
            }

        }

    }
}